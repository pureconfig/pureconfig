package pureconfig.generic

import scala.collection.JavaConverters._

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValue }
import pureconfig._
import pureconfig.error.ConfigReaderException
import shapeless._
import shapeless.labelled.FieldType

/**
 * A `ConfigWriter` for generic representations that writes values in the shape of a config object.
 *
 * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
 * @tparam Repr the generic representation
 */
trait MapShapedWriter[Wrapped, Repr] extends ConfigWriter[Repr]

object MapShapedWriter {

  implicit def labelledHNilWriter[Wrapped]: MapShapedWriter[Wrapped, HNil] = new MapShapedWriter[Wrapped, HNil] {
    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  final implicit def labelledHConsWriter[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldConvert: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[MapShapedWriter[Wrapped, T]],
    hint: ProductHint[Wrapped]): MapShapedWriter[Wrapped, FieldType[K, V] :: T] = new MapShapedWriter[Wrapped, FieldType[K, V] :: T] {

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val keyStr = hint.configKey(key.value.toString().tail)
      val rem = tConfigWriter.value.to(t.tail)
      // TODO check that all keys are unique
      vFieldConvert.value.value match {
        case f: WritesMissingKeys[V @unchecked] =>
          f.toOpt(t.head) match {
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

  implicit def cNilWriter[Wrapped]: MapShapedWriter[Wrapped, CNil] = new MapShapedWriter[Wrapped, CNil] {
    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  final implicit def cConsWriter[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[MapShapedWriter[Wrapped, T]]): MapShapedWriter[Wrapped, FieldType[Name, V] :+: T] =
    new MapShapedWriter[Wrapped, FieldType[Name, V] :+: T] {

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
}
