package pureconfig.module.spark

import scala.util.Try

import org.apache.spark.sql.types.{DataType, Metadata, StructType}

import pureconfig.ConfigConvert

/** `ConfigConvert` instances for `spark-sql` data structures.
  */
package object sql {
  implicit val dataTypeConvert: ConfigConvert[DataType] =
    ConfigConvert.viaNonEmptyStringTry[DataType](s => Try(DataType.fromDDL(s)), _.sql)

  implicit val structTypeReader: ConfigConvert[StructType] =
    ConfigConvert.viaNonEmptyStringTry[StructType](s => Try(StructType.fromDDL(s)), _.toDDL)

  implicit val metadataConvert: ConfigConvert[Metadata] =
    ConfigConvert.viaNonEmptyStringTry[Metadata](s => Try(Metadata.fromJson(s)), _.json)
}
