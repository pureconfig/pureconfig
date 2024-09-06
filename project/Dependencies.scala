import sbt._
import Utilities._

object Dependencies {

  object Version {
    val scala212 = "2.12.20"
    val scala213 = "2.13.14"
    val scala3 = "3.3.3"

    val scalaTest = "3.2.19"
    val scalaTestPlusScalaCheck = "3.2.18.0"
    val scalaCheck = "1.18.0"
  }

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-17" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
}
