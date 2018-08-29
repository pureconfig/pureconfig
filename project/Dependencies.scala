import sbt._
import sbt.Keys._

object Dependencies {

  object Version {
    val shapeless           = "2.3.3"
    val typesafeConfig      = "1.3.3"

    val scalaTest           = "3.0.5"
    val scalaCheck          = "1.13.5" // update blocked by cats-laws in the cats module
    val scalaCheckShapeless = "1.1.8"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % "test"
  val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % Version.scalaCheckShapeless % "test"
}
