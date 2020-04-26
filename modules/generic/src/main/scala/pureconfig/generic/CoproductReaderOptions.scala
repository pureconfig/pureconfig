package pureconfig.generic

import pureconfig._
import shapeless._
import shapeless.labelled._

/**
 * A typeclass to collect the `ConfigReader` options for a given coproduct, indexed by the coproduct name.
 */
private[generic] trait CoproductReaderOptions[Repr <: Coproduct] {
  def options: Map[String, ConfigReader[Repr]]
}

object CoproductReaderOptions {

  implicit val cNilReaderOptions: CoproductReaderOptions[CNil] = new CoproductReaderOptions[CNil] {
    val options: Map[String, ConfigReader[CNil]] = Map.empty
  }

  implicit def cConsReaderOptions[H, T <: Coproduct, Name <: Symbol](
    implicit
    hName: Witness.Aux[Name],
    hConfigReader: Derivation[Lazy[ConfigReader[H]]],
    tConfigReaderOptions: Lazy[CoproductReaderOptions[T]]): CoproductReaderOptions[FieldType[Name, H] :+: T] =
    new CoproductReaderOptions[FieldType[Name, H] :+: T] {
      val options = {
        val optionName = hName.value.name
        val optionReader = hConfigReader.value.value.map[FieldType[Name, H] :+: T](v => Inl(field[Name](v)))
        val remaining = tConfigReaderOptions.value.options.map {
          case (name, reader) => name -> reader.map[FieldType[Name, H] :+: T](Inr.apply)
        }
        remaining.updated(optionName, optionReader)
      }
    }
}
