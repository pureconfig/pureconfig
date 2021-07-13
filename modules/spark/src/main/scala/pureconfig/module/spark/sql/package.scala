package pureconfig.module.spark

import scala.util.Try

import org.apache.spark.sql.types.{DataType, Metadata, StructType}

import pureconfig.{ConfigConvert, ConfigReader, ConfigWriter}

/** `ConfigConvert` instances for `spark-sql` data structures.
  */
package object sql {
  implicit val dataTypeConvert: ConfigConvert[DataType] =
    ConfigConvert.viaNonEmptyStringTry[DataType](s => Try(DataType.fromDDL(s)), _.sql)

  implicit val structTypeReader: ConfigReader[StructType] =
    ConfigReader.fromNonEmptyStringTry[StructType](s => Try(StructType.fromDDL(s)))

  implicit val structTypeWriter: ConfigWriter[StructType] =
    dataTypeConvert.contramap(identity)

  implicit val metadataConvert: ConfigConvert[Metadata] =
    ConfigConvert.viaNonEmptyStringTry[Metadata](s => Try(Metadata.fromJson(s)), _.json)
}
