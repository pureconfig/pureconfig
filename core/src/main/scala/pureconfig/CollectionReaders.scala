package pureconfig

import scala.language.higherKinds
import scala.reflect.ClassTag

/** A marker trait signaling that a `ConfigReader` accepts missing (undefined) values.
  *
  * The standard behavior of `ConfigReader`s that expect required keys in config objects is to return a `KeyNotFound`
  * failure when one or more of them are missing. Mixing in this trait into the key's `ConfigReader` signals that if
  * a value is missing for the key, the `ConfigReader` can be called with a cursor in the "undefined" state.
  */
trait ReadsMissingKeys { this: ConfigReader[_] => }

/** Trait containing `ConfigReader` instances for collection types.
  */
trait CollectionReaders {

  implicit def optionReader[A](implicit conv: Derivation[ConfigReader[A]]): ConfigReader[Option[A]] =
    new ConfigReader[Option[A]] with ReadsMissingKeys {
      override def from(cur: ConfigCursor): ConfigReader.Result[Option[A]] = {
        if (cur.isUndefined || cur.isNull) Right(None)
        else conv.value.from(cur).right.map(Some(_))
      }
    }

  implicit def traversableReader[A, F[A] <: TraversableOnce[A]](implicit
      configConvert: Derivation[ConfigReader[A]],
      cbf: FactoryCompat[A, F[A]]
  ): ConfigReader[F[A]] =
    new ConfigReader[F[A]] {

      override def from(cur: ConfigCursor): ConfigReader.Result[F[A]] = {
        cur.fluent.mapList { valueCur => configConvert.value.from(valueCur) }.right.map { coll =>
          val builder = cbf.newBuilder()
          (builder ++= coll).result()
        }
      }
    }

  implicit def mapReader[A](implicit reader: Derivation[ConfigReader[A]]): ConfigReader[Map[String, A]] =
    new ConfigReader[Map[String, A]] {
      override def from(cur: ConfigCursor): ConfigReader.Result[Map[String, A]] = {
        cur.fluent.mapObject { valueCur => reader.value.from(valueCur) }
      }
    }

  implicit def arrayReader[A: ClassTag](implicit reader: Derivation[ConfigReader[A]]): ConfigReader[Array[A]] =
    new ConfigReader[Array[A]] {
      override def from(cur: ConfigCursor): ConfigReader.Result[Array[A]] =
        cur.fluent.mapList(reader.value.from).right.map(_.toArray)
    }
}

object CollectionReaders extends CollectionReaders
