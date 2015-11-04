/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import pureconfig.conf._
import pureconfig.conf.namespace._
import shapeless._
import shapeless.labelled._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.{ Failure, Success, Try }

/**
 * Trait for conversion between [[T]] and [[RawConfig]] where [[T]] is a "complex" type. For "simple"
 * types have a look at [[StringConvert]]
 */
trait ConfigConvert[T] {
  /**
   * Convert the given configuration into an instance of [[T]] if possible
   *
   * @param config The configuration from which load the config
   * @param namespace The base namespace to use for the conversion. This should be used as base
   *                  for the name of the fields of [[T]]. For instance, given a `case class Foo(i: Int)`
   *                  and a configuration `conf = Map("foo.i", "1")`, then `from(conf, "foo")` will return
   *                  `Success(Foo(1))` while `from(conf, "")` will return `Failure`
   * @return [[Success]] of [[T]] if the conversion is possible, [[Failure]] with the problem if the
   *         conversion is not
   */
  def from(config: RawConfig, namespace: String): Try[T]

  /**
   * Converts a type [[T]] to a [[RawConfig]] using the [[namespace]] as base namespace.
   *
   * @param t The instance of [[T]] to convert
   * @param namespace The base namespace. For instance, given case `class Foo(i: Int)`, then
   *                  `to(Foo(2), "base.namespace")` will return `Map("base.namespace.i", "2")`
   * @return The [[RawConfig]] obtained from the [[T]] instance
   */
  def to(t: T, namespace: String): RawConfig
}

object ConfigConvert {
  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  implicit def hNilConfigConvert = new ConfigConvert[HNil] {
    override def from(config: RawConfig, namespace: String): Try[HNil] = Success(HNil)
    override def to(t: HNil, namespace: String): RawConfig = Map.empty[String, String]
  }

  implicit def hConsConfigConvert[K <: Symbol, V, T <: HList](
    implicit key: Witness.Aux[K],
    vFieldConvert: Lazy[FieldConvert[V]],
    tConfigConvert: Lazy[ConfigConvert[T]]): ConfigConvert[FieldType[K, V] :: T] = new ConfigConvert[FieldType[K, V]:: T] {

    override def from(config: RawConfig, namespace: String): Try[FieldType[K, V] :: T] = {
      val keyStr = key.value.toString().tail // remove the ' in front of the symbol
      val namespaceWithKey = makeNamespace(namespace, keyStr)
      for {
        v <- vFieldConvert.value.from(config, namespaceWithKey)
        tail <- tConfigConvert.value.from(config, namespace)
      } yield field[K](v) :: tail
    }

    override def to(t: FieldType[K, V] :: T, namespace: String): RawConfig = {
      val keyStr = key.value.toString().tail // remove the ' in front of the symbol
      val namespaceWithKey = makeNamespace(namespace, keyStr)
      val fieldEntries = vFieldConvert.value.to(t.head, namespaceWithKey)

      // TODO check that all keys are unique
      tConfigConvert.value.to(t.tail, namespace) ++ fieldEntries
    }
  }

  case class NoValidCoproductChoiceFound(config: RawConfig, namespace: String)
    extends RuntimeException(s"No valid coproduct type choice found for $namespace in configuration $config")

  implicit def cNilConfigConvert: ConfigConvert[CNil] = new ConfigConvert[CNil] {
    override def from(config: RawConfig, namespace: String): Try[CNil] =
      Failure(NoValidCoproductChoiceFound(config, namespace))

    override def to(t: CNil, namespace: String): RawConfig = Map.empty
  }

  implicit def coproductConfigConvert[V, T <: Coproduct](
    implicit vFieldConvert: Lazy[FieldConvert[V]],
    tConfigConvert: Lazy[ConfigConvert[T]]): ConfigConvert[V :+: T] =
    new ConfigConvert[V :+: T] {

      override def from(config: RawConfig, namespace: String): Try[V :+: T] = {
        vFieldConvert.value.from(config, namespace)
          .map(s => Inl[V, T](s))
          .orElse(tConfigConvert.value.from(config, namespace).map(s => Inr[V, T](s)))
      }

      override def to(t: V :+: T, namespace: String): RawConfig = t match {
        case Inl(l) => vFieldConvert.value.to(l, namespace)
        case Inr(r) => tConfigConvert.value.to(r, namespace)
      }
    }

  // For Option[T] we use a special config converter
  implicit val deriveNone = new ConfigConvert[None.type] {
    override def from(config: RawConfig, namespace: String): Try[None.type] = {
      if (config contains namespace) Failure(new RuntimeException("None should not be found in config"))
      else Success(None)
    }
    override def to(t: None.type, namespace: String): RawConfig = Map(namespace -> "")
  }

  implicit def deriveSome[T](implicit fieldConvert: Lazy[FieldConvert[T]]) = new ConfigConvert[Some[T]] {
    override def from(config: RawConfig, namespace: String): Try[Some[T]] = {
      if (config contains namespace) fieldConvert.value.from(config, namespace).map(Some(_))
      else Failure(new RuntimeException("Some should be found in config"))
    }

    override def to(t: Some[T], namespace: String): RawConfig = fieldConvert.value.to(t.get, namespace)
  }

  // traversable of simple types, that are types with an instance of StringConvert
  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](implicit stringConvert: Lazy[StringConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {
    override def from(config: RawConfig, namespace: String): Try[F[T]] = {
      val value = config(namespace)

      if (value.isEmpty) {
        // "".split(",") returns Array("") but we want Array()
        Success(cbf().result())
      } else {
        val tryBuilder = value.split(",").foldLeft(Try(cbf())) {
          case (tryResult, rawValue) =>
            for {
              result <- tryResult
              value <- stringConvert.value.from(rawValue)
            } yield result += value
        }

        tryBuilder.map(_.result())
      }
    }

    override def to(ts: F[T], namespace: String): RawConfig =
      Map(namespace -> ts.map(stringConvert.value.to).mkString(","))
  }

  // traversable of complex types, that are types with an instance of ConfigConvert
  implicit def deriveTraversableOfObjects[T, F[T] <: TraversableOnce[T]](implicit configConvert: Lazy[ConfigConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {

    override def from(config: RawConfig, namespace: String): Try[F[T]] = {
      val fullKeysFound = config.keys.filter(_ startsWith namespace).toList
      val keysFound = fullKeysFound.map { key =>
        val parentNamespaceLength = namespace.length + namespaceSep.length
        key.substring(0, key.indexOfSlice(namespaceSep, parentNamespaceLength))
      }.sorted.distinct

      if (keysFound.isEmpty) {
        Success(cbf().result())
      } else {
        val tryBuilder = keysFound.foldLeft(Try(cbf())) {
          case (tryResult, key) =>
            for {
              result <- tryResult
              value <- configConvert.value.from(config, key)
            } yield result += value
        }

        tryBuilder.map(_.result())
      }
    }

    override def to(ts: F[T], namespace: String): RawConfig = {
      val tsWithIndexes = ts.toList.zipWithIndex // give an index/id to each element of the traversable
      val tsConverted = tsWithIndexes.map { case (t, i) => configConvert.value.to(t, makeNamespace(namespace, i.toString)) }
      tsConverted.foldLeft(Map.empty[String, String])(_ ++ _)
    }
  }

  /**
   * Extract the key from a fullKey, i.e. a namespace followed by a key. This function
   * returns an error if the key contains the [[namespaceSep]], else the key found.
   *
   * {{{
   *   scala> getMapKeyFrom("foo.bar", "foo")
   *   res0: Try[String] = Success("bar")
   *   scala> getMapKeyFrom("foo.bar.a", "foo")
   *   res1: Try[String] = Failure(..)
   * }}}
   *
   * @param fullKey The namespace plus key from which we want to extract the key
   * @param namespace The namespace
   * @return A [[Success]] with the key if it is possible to extract it, else a [[Failure]] with
   *         the error
   */
  private[this] def getMapKeyFrom(fullKey: String, namespace: String): Try[String] = {
    val key = fullKey.substring(namespace.length + namespaceSep.length)
    if (key contains namespaceSep) {
      Failure(new RuntimeException(s"Invalid Map key found '$key'. Maps keys cannot contain the namespace separator '$namespaceSep'"))
    } else {
      Success(key)
    }
  }

  implicit def deriveMap[T](implicit stringConvert: Lazy[StringConvert[T]]) = new ConfigConvert[Map[String, T]] {

    override def from(config: RawConfig, namespace: String): Try[Map[String, T]] = {
      val keysFound = config.keySet.filter(_ startsWith namespace)

      keysFound.foldLeft(Try(Map.empty[String, T])) {
        case (f @ Failure(_), _) => f
        case (Success(acc), fullKey) =>
          for {
            key <- getMapKeyFrom(fullKey, namespace)
            rawValue <- Try(config(fullKey))
            value <- stringConvert.value.from(rawValue)
          } yield acc + (key -> value)
      }
    }

    override def to(keyVals: Map[String, T], namespace: String): RawConfig = {
      keyVals.map { case (key, value) => makeNamespace(namespace, key) -> stringConvert.value.to(value) }
    }
  }

  // used for products
  implicit def deriveInstanceWithLabelledGeneric[F, Repr <: HList](
    implicit gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {

    override def from(config: RawConfig, namespace: String): Try[F] = {
      cc.value.from(config, namespace).map(gen.from)
    }

    override def to(t: F, namespace: String): RawConfig = {
      cc.value.to(gen.to(t), namespace)
    }
  }

  // used for coproducts
  implicit def deriveInstanceWithGeneric[F, Repr <: Coproduct](implicit gen: Generic.Aux[F, Repr], cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {
    override def from(config: RawConfig, namespace: String): Try[F] = {
      cc.value.from(config, namespace).map(gen.from)
    }

    override def to(t: F, namespace: String): RawConfig = {
      cc.value.to(gen.to(t), namespace)
    }
  }

}
