package pureconfig.module.spark

import scala.util.Try

import org.apache.spark.sql.types.{DataType, Metadata, StructField, StructType}

import pureconfig.{ConfigConvert, ConfigReader, ConfigWriter}

/** `ConfigConvert` instances for `spark-sql` data structures.
  */
package object sql {
  implicit val dataTypeConvert: ConfigConvert[DataType] =
    ConfigConvert.viaNonEmptyStringTry[DataType](s => Try(DataType.fromDDL(s)), _.sql)

  implicit val structTypeConvert: ConfigConvert[StructType] =
    ConfigConvert.viaNonEmptyStringTry[StructType](s => Try(StructType.fromDDL(s)), _.toDDL)

  implicit val metadataConvert: ConfigConvert[Metadata] =
    ConfigConvert.viaNonEmptyStringTry[Metadata](s => Try(Metadata.fromJson(s)), _.json)

  private val (key0, key1, key2, key3) = ("name", "data-type", "nullable", "metadata")

  implicit val structFieldReader: ConfigReader[StructField] = ConfigReader.forProduct4(key0, key1, key2, key3) {
    (name: String, dataType: DataType, maybeNullable: Option[Boolean], maybeMetadata: Option[Metadata]) =>
      (maybeNullable, maybeMetadata) match {
        case (Some(n), Some(m)) => StructField(name, dataType, n, m)
        case (Some(n), _) => StructField(name, dataType, n)
        case (_, Some(m)) => StructField(name, dataType, metadata = m)
        case _ => StructField(name, dataType)
      }
  }

  implicit val structFieldWriter: ConfigWriter[StructField] =
    ConfigWriter.forProduct4(key0, key1, key2, key3) { case StructField(name, dt, n, m) => (name, dt, n, m) }
}
