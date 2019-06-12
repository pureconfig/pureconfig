import sbt._
import sbt.Keys._

object Dependencies {

  private[this] def onScala213(onScala213: String, onOthers: String) =
    Def.setting {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => onScala213
        case _             => onOthers
      }
    }

  object Version {
    val shapeless           = "2.3.3"
    val typesafeConfig      = "1.3.4"

    val scalaTest           = "3.0.8"

    val scalaCheck          = onScala213("1.14.0", "1.13.5")
    val scalaCheckShapeless = onScala213("1.2.3", "1.1.8")
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = Def.setting {
    "org.scalacheck" %% "scalacheck" % Version.scalaCheck.value % "test"
  }
  val scalaCheckShapeless = Def.setting {
    "com.github.alexarchambault" %%
      s"scalacheck-shapeless_${onScala213("1.14", "1.13").value}" % Version.scalaCheckShapeless.value % "test"
  }
}
