package pureconfig.generic

import scala.collection.JavaConverters._

import com.typesafe.config.{ConfigList, ConfigValue, ConfigValueFactory}
import pureconfig.{ConfigWriter, Derivation}
import shapeless._

/**
  * A `ConfigWriter` for generic representations that writes values in the shape of a sequence.
  *
  * @tparam Repr the generic representation
  */
private[generic] trait SeqShapedWriter[Repr] extends ConfigWriter[Repr]

object SeqShapedWriter {

  implicit val hNilWriter: SeqShapedWriter[HNil] = new SeqShapedWriter[HNil] {
    override def to(v: HNil): ConfigValue = ConfigValueFactory.fromIterable(List().asJava)
  }

  implicit def hConsWriter[H, T <: HList](implicit
      hw: Derivation[Lazy[ConfigWriter[H]]],
      tw: Lazy[SeqShapedWriter[T]]
  ): SeqShapedWriter[H :: T] =
    new SeqShapedWriter[H :: T] {
      override def to(v: H :: T): ConfigValue = {
        tw.value.to(v.tail) match {
          case cl: ConfigList =>
            ConfigValueFactory.fromIterable((hw.value.value.to(v.head) +: cl.asScala).asJava)
          case other =>
            throw new Exception(s"Unexpected value $other when trying to write a `ConfigValue` from an `HList`.")
        }
      }
    }
}
