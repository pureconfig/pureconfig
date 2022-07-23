import sbt._

object Dependencies {

  object Version {
    val scala212 = "2.12.16"
    val scala213 = "2.13.8"
    val scala30 = "3.0.2"
    val scala31 = "3.1.3"

    val typesafeConfig = "1.4.2"

    // can't use 3.2.11 because it pulls in scala-xml 2 for Scala 2.12.
    // See https://github.com/scoverage/sbt-scoverage/issues/439
    val scalaTest = "3.2.10"
    val scalaTestPlusScalaCheck = "3.2.10.0"

    val scalaCheck = "1.16.0"
  }

  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
}
