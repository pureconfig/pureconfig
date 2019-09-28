import sbt._

object Dependencies {

  object Version {
    val scala211            = "2.11.12"
    val scala212            = "2.12.10"
    val scala213            = "2.13.0"

    val shapeless           = "2.3.3"
    val typesafeConfig      = "1.3.4"

    val scalaTest           = "3.0.8"

    val scalaCheck          = "1.14.2"
    val scalaCheckShapeless = "1.2.3"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
  val scalaCheckShapeless =
    "com.github.alexarchambault" %% s"scalacheck-shapeless_1.14" % Version.scalaCheckShapeless
}
