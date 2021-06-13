import sbt._

object Dependencies {

  object Version {
    val scala212 = "2.12.14"
    val scala213 = "2.13.6"
    val scala30 = "3.0.0"

    val shapeless = "2.3.7"
    val typesafeConfig = "1.4.1"

    val scalaTest = "3.2.9"
    val scalaTestPlusScalaCheck = "3.2.9.0"

    val scalaCheck = "1.15.4"
    val scalaCheckShapeless = "1.3.0"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
  val scalaCheckShapeless =
    "com.github.alexarchambault" %% s"scalacheck-shapeless_1.15" % Version.scalaCheckShapeless
}
