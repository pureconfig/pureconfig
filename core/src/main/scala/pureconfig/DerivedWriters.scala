package pureconfig

import scala.collection.JavaConverters._
import scala.language.higherKinds

import com.typesafe.config._
import pureconfig.error._
import shapeless._
import shapeless.labelled._

trait DerivedWriters extends DerivedWriters1 {
  implicit def deriveAnyVal[T, U](
    implicit
    ev: T <:< AnyVal,
    generic: Generic[T],
    unwrapped: Unwrapped.Aux[T, U],
    writer: ConfigWriter[U]): ConfigWriter[T] =
    new ConfigWriter[T] {
      override def to(t: T): ConfigValue = writer.to(unwrapped.unwrap(t))
    }

  // used for tuples
  implicit final def deriveTupleInstance[F: IsTuple, Repr](
    implicit
    gen: Generic.Aux[F, Repr],
    cc: ConfigWriter[Repr]): ConfigWriter[F] = new ConfigWriter[F] {
    override def to(t: F): ConfigValue =
      cc.to(gen.to(t))
  }
}

/**
 * Trait containing `ConfigWriter` instances for collection, product and coproduct types.
 */
trait DerivedWriters1 {

  private[pureconfig] trait WrappedConfigWriter[Wrapped, SubRepr] {
    def to(cv: SubRepr): ConfigValue
  }

  implicit final def labelledHNilConfigWriter[Wrapped]: WrappedConfigWriter[Wrapped, HNil] = new WrappedConfigWriter[Wrapped, HNil] {
    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit final def labelledHConsConfigWriter[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldConvert: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[WrappedConfigWriter[Wrapped, T]],
    hint: ProductHint[Wrapped]): WrappedConfigWriter[Wrapped, FieldType[K, V] :: T] = new WrappedConfigWriter[Wrapped, FieldType[K, V] :: T] {

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val keyStr = hint.configKey(key.value.toString().tail)
      val rem = tConfigWriter.value.to(t.tail)
      // TODO check that all keys are unique
      vFieldConvert.value.value match {
        case f: OptionConfigWriter[_] =>
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

  implicit final def cNilConfigWriter[Wrapped]: WrappedConfigWriter[Wrapped, CNil] = new WrappedConfigWriter[Wrapped, CNil] {
    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit final def coproductConfigWriter[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[WrappedConfigWriter[Wrapped, T]]): WrappedConfigWriter[Wrapped, FieldType[Name, V] :+: T] =
    new WrappedConfigWriter[Wrapped, FieldType[Name, V] :+: T] {

      override def to(t: FieldType[Name, V] :+: T): ConfigValue = t match {
        case Inl(l) =>
          // Writing a coproduct to a config can fail. Is it worth it to make `to` return a `Try`?
          coproductHint.to(vFieldConvert.value.value.to(l), vName.value.name) match {
            case Left(failures) => throw new ConfigReaderException[FieldType[Name, V] :+: T](failures)
            case Right(r) => r
          }

        case Inr(r) =>
          tConfigWriter.value.to(r)
      }
    }

  implicit def deriveOption[T](implicit conv: Derivation[Lazy[ConfigWriter[T]]]) = new OptionConfigWriter[T]

  class OptionConfigWriter[T](implicit conv: Derivation[Lazy[ConfigWriter[T]]]) extends ConfigWriter[Option[T]] {
    override def to(t: Option[T]): ConfigValue = t match {
      case Some(v) => conv.value.value.to(v)
      case None => ConfigValueFactory.fromAnyRef(null)
    }

    def toOption(t: Option[T]): Option[ConfigValue] = t.map(conv.value.value.to)
  }

  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[Lazy[ConfigWriter[T]]]) = new ConfigWriter[F[T]] {

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.value.to).asJava)
    }
  }

  implicit def deriveMap[T](implicit configConvert: Derivation[Lazy[ConfigWriter[T]]]) = new ConfigWriter[Map[String, T]] {
    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.value.to).asJava)
    }
  }

  implicit final lazy val hNilConfigWriter: ConfigWriter[HNil] = new ConfigWriter[HNil] {
    override def to(v: HNil): ConfigValue = ConfigValueFactory.fromIterable(List().asJava)
  }

  implicit final def hConsConfigWriter[H, T <: HList](implicit hw: ConfigWriter[H], tw: ConfigWriter[T]): ConfigWriter[H :: T] =
    new ConfigWriter[H :: T] {
      override def to(v: (H :: T)): ConfigValue = {
        tw.to(v.tail) match {
          case cl: ConfigList =>
            ConfigValueFactory.fromIterable((hw.to(v.head) +: cl.asScala).asJava)
          case other =>
            throw new Exception(s"Unexpected value $other when trying to write a `ConfigValue` from an `HList`.")
        }
      }
    }

  // used for both products and coproducts
  implicit final def deriveLabelledGenericInstance[F, Repr](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[WrappedConfigWriter[F, Repr]]): ConfigWriter[F] = new ConfigWriter[F] {

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }
}

object DerivedWriters extends DerivedWriters
