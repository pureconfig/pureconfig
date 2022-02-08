import sbt._

object Dependencies {

  object Version {
    val scala212 = "2.12.15"
    val scala213 = "2.13.8"
    val scala30 = "3.0.2"
    val scala31 = "3.1.1"

    val typesafeConfig = "1.4.2"

    val scalaTest = "3.2.11"
    val scalaTestPlusScalaCheck = "3.2.11.0"

    val scalaCheck = "1.15.4"
  }

  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
}
