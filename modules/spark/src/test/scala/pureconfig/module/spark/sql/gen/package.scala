package pureconfig.module.spark.sql

import org.apache.spark.sql.types._
import org.json4s._
import org.scalacheck.{Arbitrary, Gen}

package object gen {
  // Valid `Metadata` field types: Boolean, Long, Double, String, Metadata, Array[Boolean], Array[Long], Array[Double], Array[String], and Array[Metadata]
  val genJBool: Gen[JBool] = Arbitrary.arbitrary[Boolean].map(JBool.apply)
  val genJLong: Gen[JLong] = Arbitrary.arbitrary[Long].map(JLong.apply)
  val genJDouble: Gen[JDouble] = Arbitrary.arbitrary[Double].map(JDouble.apply)
  val genJString: Gen[JString] = Arbitrary.arbitrary[String].map(JString.apply)

  val genJPrimitive: Gen[JValue] = Gen.oneOf(genJBool, genJLong, genJDouble, genJString)

  def genJField(j: Gen[JValue]): Gen[JField] =
    for {
      k <- Gen.nonEmptyBuildableOf[String, Char](
        Gen.oneOf(Gen.alphaLowerChar, Gen.alphaNumChar, Gen.const('_'), Gen.const('-'))
      )
      v <- j
    } yield JField(k, v)

  def genJObject(maxDepth: Int, maxWidth: Int): Gen[JObject] = (maxDepth match {
    case d if d <= 0 => Gen.listOfN(maxWidth, genJField(genJPrimitive))
    case d =>
      Gen.listOfN(
        maxWidth,
        genJField(Gen.oneOf(genJPrimitive, genJArray(maxWidth, d - 1), genJObject(maxWidth, d - 1)))
      )
  }).map(JObject(_))

  // `Metadata` arrays cannot be nested
  def genJArray(maxWidth: Int, maxDepth: Int): Gen[JArray] = {
    val x = Gen.listOfN(maxWidth, genJBool)
    val y = Gen.listOfN(maxWidth, genJLong)
    val xs = (genJDouble :: genJString :: (if (maxDepth <= 0) Nil else genJObject(maxWidth, maxDepth - 1) :: Nil))
      .map(Gen.listOfN(maxWidth, _))

    Gen.oneOf(x, y, xs: _*).map(JArray.apply)
  }

  // max precision, scale = 38
  // scale <= precision
  // scale >= 0
  val genDecimalType: Gen[DecimalType] =
    for {
      p <- Gen.chooseNum(0, DecimalType.MAX_PRECISION)
      s <- Gen.chooseNum(0, p)
    } yield DecimalType(p, s)

  val genPrimitiveType: Gen[DataType] =
    Gen.oneOf(Gen.oneOf(StringType, IntegerType, BooleanType, DoubleType), genDecimalType)

  def genStructField(tGen: Gen[DataType]): Gen[StructField] =
    for {
      n <- Gen.identifier
      t <- tGen
    } yield StructField(n, t)

  def genStructType(maxDepth: Int, maxWidth: Int): Gen[StructType] = (maxDepth match {
    case d if d <= 0 => Gen.listOfN(maxWidth, genStructField(genPrimitiveType))
    case d =>
      Gen.listOfN(
        maxWidth,
        genStructField(
          Gen.oneOf(genPrimitiveType, genStructType(maxWidth, d - 1), genArrayType(maxWidth, d - 1))
        )
      )
  }).map(StructType.apply)

  def genArrayType(maxDepth: Int, maxWidth: Int): Gen[ArrayType] =
    for {
      t <-
        if (maxDepth <= 0) genPrimitiveType
        else
          Gen.oneOf(
            genPrimitiveType,
            genStructType(maxWidth, maxDepth - 1),
            genArrayType(maxWidth, maxDepth - 1)
          )
    } yield ArrayType(t)
}
