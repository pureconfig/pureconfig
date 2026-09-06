import sbt._
import Utilities._

object Dependencies {

  object Version {
    val scala213 = "3.9.0"
    val scala3 = "3.3.8"

    val scalaTest = "3.2.20"
    val scalaTestPlusScalaCheck = "3.2.20.0"
    val scalaCheck = "1.20.0"
  }

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-19" % Version.scalaTestPlusScalaCheck
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
}
