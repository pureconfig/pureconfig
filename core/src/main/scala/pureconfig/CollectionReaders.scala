package pureconfig

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

import pureconfig.ConvertHelpers._
import pureconfig.error._

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
      override def from(cur: ConfigCursor): Either[ConfigReaderFailures, Option[T]] = {
        if (cur.isUndefined || cur.isNull) Right(None)
        else conv.value.from(cur).right.map(Some(_))
      }
    }

  implicit def traversableReader[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[ConfigReader[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigReader[F[T]] {

    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F[T]] = {
      cur.asCollectionCursor.right.flatMap {
        case Left(listCur) =>
          // we called all the failures in the list
          listCur.list.foldLeft[Either[ConfigReaderFailures, mutable.Builder[T, F[T]]]](Right(cbf())) {
            case (acc, valueCur) =>
              combineResults(acc, configConvert.value.from(valueCur))(_ += _)
          }.right.map(_.result())

        case Right(objCur) =>
          def keyValueReader(key: String, valueCur: ConfigCursor): Either[ConfigReaderFailures, (Int, T)] = {
            val keyResult = catchReadError(_.toInt)(implicitly)(key)
              .left.flatMap { t =>
                valueCur.failed(CannotConvert(key, "Int", "To convert an object to a collection, its keys must be " +
                  s"read as integers but key $key is not a valid one. Error: ${t.description}"))
              }
            val valueResult = configConvert.value.from(valueCur)
            combineResults(keyResult, valueResult)(_ -> _)
          }

          objCur.map.foldLeft[Either[ConfigReaderFailures, List[(Int, T)]]](Right(Nil)) {
            case (acc, (str, v)) =>
              combineResults(acc, keyValueReader(str, v))(_ :+ _)
          }.right.map { l =>
            val r = cbf()
            r ++= l.sortBy(_._1).map(_._2)
            r.result()
          }
      }
    }
  }

  implicit def mapReader[T](implicit reader: Derivation[ConfigReader[T]]) = new ConfigReader[Map[String, T]] {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, Map[String, T]] = {
      cur.asMap.right.flatMap { map =>
        map.foldLeft[Either[ConfigReaderFailures, Map[String, T]]](Right(Map())) {
          case (acc, (key, valueConf)) =>
            combineResults(acc, reader.value.from(valueConf)) { (map, value) => map + (key -> value) }
        }
      }
    }
  }
}

object CollectionReaders extends CollectionReaders
