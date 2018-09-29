package pureconfig

import scala.collection.JavaConverters._
import scala.language.higherKinds

import com.typesafe.config._

/**
 * Trait containing `ConfigWriter` instances for collection types.
 */
trait CollectionWriters {

  implicit def optionWriter[T](implicit conv: Derivation[ConfigWriter[T]]) = new CollectionWriters.OptionConfigWriter[T]

  implicit def traversableWriter[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[ConfigWriter[T]]) = new ConfigWriter[F[T]] {

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.to).asJava)
    }
  }

  implicit def mapWriter[T](implicit configConvert: Derivation[ConfigWriter[T]]) = new ConfigWriter[Map[String, T]] {
    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).asJava)
    }
  }
}

object CollectionWriters extends CollectionWriters {

  // TODO: change this to an `AllowMissingKey`-like trait for better extensibility
  class OptionConfigWriter[T](implicit conv: Derivation[ConfigWriter[T]]) extends ConfigWriter[Option[T]] {
    override def to(t: Option[T]): ConfigValue = t match {
      case Some(v) => conv.value.to(v)
      case None => ConfigValueFactory.fromAnyRef(null)
    }

    def toOption(t: Option[T]): Option[ConfigValue] = t.map(conv.value.to)
  }
}
