import Utilities._
import sbt._

object Dependencies {

  object Version {
    val shapeless           = "2.3.3"
    val typesafeConfig      = "1.3.4"

    val scalaTest           = "3.0.8"

    val scalaCheck          = "1.14.1"
    val scalaCheckShapeless = "1.2.3"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % "test"
  val scalaCheckShapeless =
    "com.github.alexarchambault" %% s"scalacheck-shapeless_1.14" % Version.scalaCheckShapeless % "test"
}
