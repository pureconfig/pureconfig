/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import com.typesafe.config._
import shapeless._
import shapeless.labelled._

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.{ Failure, Success, Try }

import java.net.URL
import pureconfig.ConfigConvert.{ fromString, stringConvert }

/**
 * Trait for conversion between `T` and `ConfigValue`.
 */
trait ConfigConvert[T] {
  /**
   * Convert the given configuration into an instance of `T` if possible.
   *
   * @param config The configuration from which load the config
   * @return `Success` of `T` if the conversion is possible, `Failure` with the problem if the
   *         conversion is not
   */
  def from(config: ConfigValue): Try[T]

  /**
   * Converts a type `T` to a `ConfigValue`.
   *
   * @param t The instance of `T` to convert
   * @return The `ConfigValue` obtained from the `T` instance
   */
  def to(t: T): ConfigValue
}

object ConfigConvert extends LowPriorityConfigConvertImplicits {
  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  private def fromFConvert[T](fromF: String => T): ConfigValue => Try[T] =
    config => {
      Try(fromF(config.valueType() match {
        case ConfigValueType.STRING => config.unwrapped().toString
        case _ => config.render(ConfigRenderOptions.concise())
      }))
    }

  def fromString[T](fromF: String => T): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Try[T] = fromFConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def stringConvert[T](fromF: String => T, toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Try[T] = fromFConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  implicit def hNilConfigConvert = new ConfigConvert[HNil] {
    override def from(config: ConfigValue): Try[HNil] = Success(HNil)
    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit def hConsConfigConvert[K <: Symbol, V, T <: HList](
    implicit key: Witness.Aux[K],
    vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[ConfigConvert[T]]): ConfigConvert[FieldType[K, V] :: T] = new ConfigConvert[FieldType[K, V]:: T] {

    override def from(config: ConfigValue): Try[FieldType[K, V] :: T] = {
      config match {
        case co: ConfigObject =>
          val keyStr = key.value.toString().tail // remove the ' in front of the symbol
          for {
            v <- vFieldConvert.value.from(co.get(keyStr))
            tail <- tConfigConvert.value.from(config)
          } yield field[K](v) :: tail
        case other =>
          Failure(new Exception(s"Couldn't derive hlist from $other."))
      }
    }

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val keyStr = key.value.toString().tail
      val rem = tConfigConvert.value.to(t.tail)
      // TODO check that all keys are unique
      vFieldConvert.value match {
        case f: OptionConfigConvert[_] =>
          f.toOption(t.head) match {
            case Some(v) =>
              rem.asInstanceOf[ConfigObject].withValue(keyStr, v)
            case None =>
              rem
          }
        case f =>
          val fieldEntry = f.to(t.head)
          rem.asInstanceOf[ConfigObject].withValue(keyStr, fieldEntry)
      }
    }
  }

  case class NoValidCoproductChoiceFound(config: ConfigValue)
    extends RuntimeException(s"No valid coproduct type choice found for configuration $config")

  implicit def cNilConfigConvert: ConfigConvert[CNil] = new ConfigConvert[CNil] {
    override def from(config: ConfigValue): Try[CNil] =
      Failure(NoValidCoproductChoiceFound(config))

    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit def coproductConfigConvert[V, T <: Coproduct](
    implicit vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[ConfigConvert[T]]): ConfigConvert[V :+: T] =
    new ConfigConvert[V :+: T] {

      override def from(config: ConfigValue): Try[V :+: T] = {
        vFieldConvert.value.from(config)
          .map(s => Inl[V, T](s))
          .orElse(tConfigConvert.value.from(config).map(s => Inr[V, T](s)))
      }

      override def to(t: V :+: T): ConfigValue = t match {
        case Inl(l) => vFieldConvert.value.to(l)
        case Inr(r) => tConfigConvert.value.to(r)
      }
    }

  // For Option[T] we use a special config converter
  implicit def deriveOption[T](implicit conv: Lazy[ConfigConvert[T]]) = new OptionConfigConvert[T]

  class OptionConfigConvert[T](implicit conv: Lazy[ConfigConvert[T]]) extends ConfigConvert[Option[T]] {
    override def from(config: ConfigValue): Try[Option[T]] = {
      if (config == null || config.unwrapped() == null)
        Success(None)
      else
        conv.value.from(config).map(Some(_))
    }

    override def to(t: Option[T]): ConfigValue = t match {
      case Some(v) => conv.value.to(v)
      case None => ConfigValueFactory.fromMap(Map().asJava)
    }

    def toOption(t: Option[T]): Option[ConfigValue] = t.map(conv.value.to)
  }

  // traversable of types with an instance of ConfigConvert
  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](implicit configConvert: Lazy[ConfigConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {

    override def from(config: ConfigValue): Try[F[T]] = {
      config match {
        case co: ConfigList =>
          val tryBuilder = co.asScala.foldLeft(Try(cbf())) {
            case (tryResult, v) =>
              for {
                result <- tryResult
                value <- configConvert.value.from(v)
              } yield result += value
          }

          tryBuilder.map(_.result())
        case other =>
          Failure(new Exception(s"Couldn't derive traversable from $other."))
      }
    }

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.to).asJava)
    }
  }

  implicit def deriveMap[T](implicit configConvert: Lazy[ConfigConvert[T]]) = new ConfigConvert[Map[String, T]] {

    override def from(config: ConfigValue): Try[Map[String, T]] = {
      config match {
        case co: ConfigObject =>
          val keysFound = co.keySet().asScala.toList

          keysFound.foldLeft(Try(Map.empty[String, T])) {
            case (f @ Failure(_), _) => f
            case (Success(acc), key) =>
              for {
                rawValue <- Try(co.get(key))
                value <- configConvert.value.from(rawValue)
              } yield acc + (key -> value)
          }
        case other =>
          Failure(new Exception(s"Couldn't derive map from $other."))
      }
    }

    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).asJava)
    }
  }

  // used for products
  implicit def deriveInstanceWithLabelledGeneric[F, Repr <: HList](
    implicit gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {

    override def from(config: ConfigValue): Try[F] = {
      cc.value.from(config).map(gen.from)
    }

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }

  // used for coproducts
  implicit def deriveInstanceWithGeneric[F, Repr <: Coproduct](implicit gen: Generic.Aux[F, Repr], cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {
    override def from(config: ConfigValue): Try[F] = {
      cc.value.from(config).map(gen.from)
    }

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }
}

/**
 * Implicit [[ConfigConvert]] instances defined such that they can be overriden by library consumer via a locally defined implementation.
 */
trait LowPriorityConfigConvertImplicits {
  import scala.concurrent.duration.{ Duration, FiniteDuration }
  implicit val durationConfigConvert: ConfigConvert[Duration] = new ConfigConvert[Duration] {
    override def from(config: ConfigValue): Try[Duration] = {
      Some(config.render(ConfigRenderOptions.concise())).fold[Try[Duration]](Failure(new Exception(s"Couldn't read duration from $config."))) { durationString =>
        DurationConvert.from(durationString).recoverWith {
          case ex => Failure(new Exception(s"Could not parse a duration from '$durationString'. (try ns, us, ms, s, m, h, d)"))
        }
      }
    }
    override def to(t: Duration): ConfigValue = {
      ConfigValueFactory.fromAnyRef(DurationConvert.from(t))
    }
  }

  implicit val finiteDurationConfigConvert: ConfigConvert[FiniteDuration] = new ConfigConvert[FiniteDuration] {
    override def from(config: ConfigValue): Try[FiniteDuration] = durationConfigConvert.from(config) match {
      case Success(v) if v.isFinite() => Success(Duration(v.length, v.unit))
      case _ => Failure(new Exception(s"Couldn't derive a finite duration from '$config'"))
    }
    override def to(t: FiniteDuration): ConfigValue = durationConfigConvert.to(t)
  }

  implicit val readString = fromString[String](identity)
  implicit val readBoolean = fromString[Boolean](_.toBoolean)
  implicit val readDouble = fromString[Double](_.toDouble)
  implicit val readFloat = fromString[Float](_.toFloat)
  implicit val readInt = fromString[Int](_.toInt)
  implicit val readLong = fromString[Long](_.toLong)
  implicit val readShort = fromString[Short](_.toShort)
  implicit val readURL = stringConvert[URL](new URL(_), _.toString)
}
