import sbt._

object Dependencies {

  object Version {
    val scala212 = "2.12.12"
    val scala213 = "2.13.5"
    val scala30 = "3.0.0-RC1"

    val shapeless = "2.3.4"
    val typesafeConfig = "1.4.1"

    val scalaTest = "3.2.6"
    val scalaTestPlusScalaCheck = "3.2.7.0"

    val scalaCheck = "1.15.3"
    val scalaCheckShapeless = "1.2.5"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
  val scalaCheckShapeless =
    "com.github.alexarchambault" %% s"scalacheck-shapeless_1.14" % Version.scalaCheckShapeless
}
