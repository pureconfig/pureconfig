package pureconfig.module.spark.sql

import com.typesafe.config.ConfigValueFactory
import org.apache.spark.sql.types._

import pureconfig.BaseSuite
import pureconfig.error.CannotConvert
import pureconfig.module.spark.sql.arbitrary._

class SparkSqlSuite extends BaseSuite {
  checkArbitrary[Metadata]
  checkArbitrary[DataType]
  checkArbitrary[StructType]
  checkArbitrary[StructField]

  //check empty metadata
  checkReadWriteString[Metadata]("{}" -> Metadata.empty)

  //check empty array
  checkReadWriteString[Metadata]("{\"k\":[]}" -> Metadata.fromJson("{\"k\":[]}"))

  //check null
  checkReadWriteString[Metadata]("{\"k\":null}" -> Metadata.fromJson("{\"k\":null}"))

  //check array with null
  checkFailure[Metadata, CannotConvert](ConfigValueFactory.fromAnyRef("{\"k\":[1,null]}"))

  //check array of mixed types
  checkFailure[Metadata, CannotConvert](ConfigValueFactory.fromAnyRef("{\"k\": [1, \"2\", 3.5] }"))

  //check nested arrays
  checkFailure[Metadata, CannotConvert](ConfigValueFactory.fromAnyRef("{\"k\": [[1, 2, 3], [4, 5, 6]] }"))

  //check decimal with out of bound precision/scale
  checkFailure[DataType, CannotConvert](ConfigValueFactory.fromAnyRef("decimal(100, 100)"))
}
