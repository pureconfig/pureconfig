package pureconfig.generic

import scala.collection.JavaConverters._

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValue }
import pureconfig._
import shapeless._
import shapeless.labelled.FieldType

/**
 * A `ConfigWriter` for generic representations that writes values in the shape of a config object.
 *
 * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
 * @tparam Repr the generic representation
 */
private[generic] trait MapShapedWriter[Wrapped, Repr] extends ConfigWriter[Repr]

object MapShapedWriter {

  implicit def labelledHNilWriter[Wrapped]: MapShapedWriter[Wrapped, HNil] = new MapShapedWriter[Wrapped, HNil] {
    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  final implicit def labelledHConsWriter[Wrapped, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    hConfigWriter: Derivation[Lazy[ConfigWriter[H]]],
    tConfigWriter: Lazy[MapShapedWriter[Wrapped, T]],
    hint: ProductHint[Wrapped]): MapShapedWriter[Wrapped, FieldType[K, H] :: T] =
    new MapShapedWriter[Wrapped, FieldType[K, H] :: T] {
      override def to(t: FieldType[K, H] :: T): ConfigValue = {
        val rem = tConfigWriter.value.to(t.tail)
        val valueOpt = hConfigWriter.value.value match {
          case tc: WritesMissingKeys[H @unchecked] =>
            tc.toOpt(t.head)
          case w =>
            Some(w.to(t.head))
        }
        val kv = hint.to(valueOpt, key.value.name)

        // TODO check that all keys are unique
        kv.fold(rem) {
          case (k, v) =>
            rem.asInstanceOf[ConfigObject].withValue(k, v)
        }
      }
    }
}
