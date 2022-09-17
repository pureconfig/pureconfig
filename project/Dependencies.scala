import sbt._
import Utilities._

object Dependencies {

  object Version {
    val scala212 = "2.12.17"
    val scala213 = "2.13.8"
    val scala3 = "3.2.0"

    val typesafeConfig = "1.4.2"

    // can't use 3.2.11+ on Scala 2.12 because it pulls in scala-xml 2.
    // See https://github.com/scoverage/sbt-scoverage/issues/439
    val scalaTest212 = "3.2.13"
    val scalaTestPlusScalaCheck212 = "3.2.10.0"
    val scalaTest = "3.2.12"
    val scalaTestPlusScalaCheck = "3.2.11.0"

    val scalaCheck = "1.17.0"
  }

  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = forScalaVersions {
    case (2, 12) => "org.scalatest" %% "scalatest" % Version.scalaTest212
    case _ => "org.scalatest" %% "scalatest" % Version.scalaTest
  }

  val scalaTestPlusScalaCheck = forScalaVersions {
    case (2, 12) => "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck212
    case _ => "org.scalatestplus" %% "scalacheck-1-15" % Version.scalaTestPlusScalaCheck
  }

  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
}
