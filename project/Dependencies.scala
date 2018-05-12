import sbt._
import sbt.Keys._

object Dependencies {

  object Version {
    private[this] def onScala210(onScala210: String, onOthers: String) = Def.setting {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => onScala210
        case _ => onOthers
      }
    }

    val shapeless           = onScala210("2.3.2", "2.3.3") // https://github.com/milessabin/shapeless/issues/831
    val scalaMacrosParadise = "2.1.1"
    val typesafeConfig      = "1.3.3"

    val scalaTest           = "3.0.5"
    val scalaCheck          = "1.13.5" // update blocked by cats-laws in the cats module
    val scalaCheckShapeless = onScala210("1.1.7", "1.1.8") // https://github.com/milessabin/shapeless/issues/831
  }

  val shapeless = Def.setting { "com.chuusai" %% "shapeless" % Version.shapeless.value }
  val scalaMacrosParadise = compilerPlugin("org.scalamacros" % "paradise" % Version.scalaMacrosParadise cross CrossVersion.patch)
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % "test"
  val scalaCheckShapeless = Def.setting { "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % Version.scalaCheckShapeless.value % "test" }
}
