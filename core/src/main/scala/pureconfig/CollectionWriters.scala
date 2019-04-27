package pureconfig

import scala.collection.JavaConverters._
import scala.language.higherKinds

import com.typesafe.config._

/**
 * A trait signaling that a `ConfigWriter` can write missing (undefined) values.
 *
 * `ConfigWriter`s always produce a valid `ConfigValue` with their `to` method. This trait adds an extra `toOpt`
 * method that parent writers can use in order to decide whether or not they should write a value using this writer.
 */
trait WritesMissingKeys[A] { this: ConfigWriter[A] =>
  def toOpt(a: A): Option[ConfigValue]
}

/**
 * Trait containing `ConfigWriter` instances for collection types.
 */
trait CollectionWriters {

  implicit def optionWriter[T](implicit conv: Derivation[ConfigWriter[T]]): ConfigWriter[Option[T]] =
    new ConfigWriter[Option[T]] with WritesMissingKeys[Option[T]] {
      override def to(t: Option[T]): ConfigValue = t match {
        case Some(v) => conv.value.to(v)
        case None => ConfigValueFactory.fromAnyRef(null)
      }

      def toOpt(t: Option[T]): Option[ConfigValue] = t.map(conv.value.to)
    }

  implicit def traversableWriter[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[ConfigWriter[T]]) = new ConfigWriter[F[T]] {

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.to).asJava)
    }
  }

  implicit def mapWriter[T](implicit configConvert: Derivation[ConfigWriter[T]]) = new ConfigWriter[Map[String, T]] {
    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).toMap.asJava)
    }
  }
}

object CollectionWriters extends CollectionWriters
