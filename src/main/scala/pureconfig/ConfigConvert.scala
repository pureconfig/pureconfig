/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import pureconfig.conf._
import shapeless.labelled._
import shapeless._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.{ Failure, Success, Try }

import conf.namespace._

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

  implicit val deriveNil = new ConfigConvert[Nil.type] {
    override def from(config: RawConfig, namespace: String): Try[Nil.type] = Success(Nil)
    override def to(t: Nil.type, namespace: String): RawConfig = Map(namespace -> "")
  }

  implicit def deriveTraversable[T, F[_] <: TraversableOnce[_]](implicit stringConvert: Lazy[StringConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {
    override def from(config: RawConfig, namespace: String): Try[F[T]] = {
      val value = config(namespace)

      if (value.isEmpty) {
        // "".split(",") returns Array("") but we want Array()
        Success(cbf().result())
      } else {
        val tryBuilder = config(namespace).split(",").foldLeft(Try(cbf())) {
          case (tryResult, rawValue) =>
            for {
              result <- tryResult
              value <- stringConvert.value.from(rawValue)
            } yield result += value
        }

        tryBuilder.map(_.result())
      }
    }

    override def to(t: F[T], namespace: String): RawConfig =
      Map(namespace -> t.mkString(","))
  }

  // used for products
  implicit def deriveInstanceWithLabelledGeneric[F, Repr](
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
  implicit def deriveInstanceWithGeneric[F, Repr](implicit gen: Generic.Aux[F, Repr], cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {
    override def from(config: RawConfig, namespace: String): Try[F] = {
      cc.value.from(config, namespace).map(gen.from)
    }

    override def to(t: F, namespace: String): RawConfig = {
      cc.value.to(gen.to(t), namespace)
    }
  }

}
