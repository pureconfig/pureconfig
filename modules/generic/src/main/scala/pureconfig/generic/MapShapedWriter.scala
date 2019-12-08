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
    vFieldWriter: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[MapShapedWriter[Wrapped, T]],
    hint: ProductHint[Wrapped]): MapShapedWriter[Wrapped, FieldType[K, V] :: T] = new MapShapedWriter[Wrapped, FieldType[K, V] :: T] {

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val rem = tConfigWriter.value.to(t.tail)
      val valueOpt = vFieldWriter.value.value match {
        case w: WritesMissingKeys[V @unchecked] =>
          w.toOpt(t.head)
        case w =>
          Some(w.to(t.head))
      }
      val kv = hint.to(key.value.name, valueOpt)

      // TODO check that all keys are unique
      kv.fold(rem) {
        case (k, v) =>
          rem.asInstanceOf[ConfigObject].withValue(k, v)
      }
    }
  }

  implicit def cNilWriter[Wrapped]: MapShapedWriter[Wrapped, CNil] = new MapShapedWriter[Wrapped, CNil] {
    override def to(t: CNil): ConfigValue =
      throw new IllegalStateException("Cannot encode CNil. This is likely a bug in PureConfig.")
  }

  final implicit def cConsWriter[Wrapped, Name <: Symbol, V <: Wrapped, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vConfigWriter: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[MapShapedWriter[Wrapped, T]]): MapShapedWriter[Wrapped, FieldType[Name, V] :+: T] =
    new MapShapedWriter[Wrapped, FieldType[Name, V] :+: T] {

      override def to(t: FieldType[Name, V] :+: T): ConfigValue = t match {
        case Inl(l) =>
          // Writing a coproduct to a config can fail. Is it worth it to make `to` return a `Try`?
          coproductHint.to(vName.value.name, vConfigWriter.value.value.to(l)) match {
            case Left(failures) => throw new ConfigReaderException[FieldType[Name, V] :+: T](failures)
            case Right(r) => r
          }

        case Inr(r) =>
          tConfigWriter.value.to(r)
      }
    }
}
