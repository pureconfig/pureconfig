package pureconfig.module.spark.sql

import org.apache.spark.sql.types._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.scalacheck.{Arbitrary, Gen}

import pureconfig.module.spark.sql.gen._

package object arbitrary {
  implicit val arbMetadata: Arbitrary[Metadata] = Arbitrary(
    genJObject(5, 5).map(j => Metadata.fromJson(compact(render(j))))
  )
  implicit val arbStructType: Arbitrary[StructType] = Arbitrary(genStructType(5, 5))
  implicit val arbDataType: Arbitrary[DataType] = Arbitrary(
    Gen.oneOf(genPrimitiveType, arbStructType.arbitrary, genArrayType(5, 5))
  )
  implicit val arbStructField: Arbitrary[StructField] = Arbitrary(genStructField(arbDataType.arbitrary))
}
