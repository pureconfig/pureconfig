package pureconfig

import scala.language.higherKinds

/**
 * A marker trait signaling that a `ConfigReader` accepts missing (undefined) values.
 *
 * The standard behavior of `ConfigReader`s that expect required keys in config objects is to return a `KeyNotFound`
 * failure when one or more of them are missing. Mixing in this trait into the key's `ConfigReader` signals that if
 * a value is missing for the key, the `ConfigReader` can be called with a cursor in the "undefined" state.
 */
trait ReadsMissingKeys { this: ConfigReader[_] => }

/**
 * Trait containing `ConfigReader` instances for collection types.
 */
trait CollectionReaders {

  implicit def optionReader[T](implicit conv: Derivation[ConfigReader[T]]): ConfigReader[Option[T]] =
    new ConfigReader[Option[T]] with ReadsMissingKeys {
      override def from(cur: ConfigCursor): ConfigReader.Result[Option[T]] = {
        if (cur.isUndefined || cur.isNull) Right(None)
        else conv.value.from(cur).right.map(Some(_))
      }
    }

  implicit def traversableReader[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[ConfigReader[T]],
    cbf: FactoryCompat[T, F[T]]) = new ConfigReader[F[T]] {

    override def from(cur: ConfigCursor): ConfigReader.Result[F[T]] = {
      cur.fluent.mapList { valueCur => configConvert.value.from(valueCur) }.right.map { coll =>
        val builder = cbf.newBuilder()
        (builder ++= coll).result()
      }
    }
  }

  implicit def mapReader[T](implicit reader: Derivation[ConfigReader[T]]) = new ConfigReader[Map[String, T]] {
    override def from(cur: ConfigCursor): ConfigReader.Result[Map[String, T]] = {
      cur.fluent.mapObject { valueCur => reader.value.from(valueCur) }
    }
  }
}

object CollectionReaders extends CollectionReaders
