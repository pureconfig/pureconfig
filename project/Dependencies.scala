import Utilities._
import sbt._

object Dependencies {

  object Version {
    val shapeless           = "2.3.3"
    val typesafeConfig      = "1.3.4"

    val scalaTest           = "3.0.8"

    // cats will only be compatible with scalacheck 1.14 on 2.x
    val scalaCheck          = forScalaVersions { case (2, 13) => "1.14.0"; case _ => "1.13.5" }
    val scalaCheckShapeless = forScalaVersions { case (2, 13) => "1.2.3"; case _ => "1.1.8" }
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  val scalaCheck = Def.setting { "org.scalacheck" %% "scalacheck" % Version.scalaCheck.value }
  val scalaCheckShapeless = Def.setting {
    "com.github.alexarchambault" %%
      s"scalacheck-shapeless_${forScalaVersions { case (2, 13) => "1.14"; case _ => "1.13" }.value}" %
      Version.scalaCheckShapeless.value
  }
}
