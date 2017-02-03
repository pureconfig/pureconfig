import sbt._

object Dependencies {

  object Version {
    val shapeless           = "2.3.2"
    val scalaMacrosParadise = "2.1.0"
    val typesafeConfig      = "1.3.1"
    val scalaTest           = "3.0.0"
    val joda                = "2.9.7"
    val jodaConvert         = "1.8"
    val scalaCheck          = "1.13.4"
    val scalaCheckShapeless = "1.1.3"
  }

  val shapeless = "com.chuusai" %% "shapeless" % Version.shapeless
  val scalaMacrosParadise = compilerPlugin("org.scalamacros" % "paradise" % Version.scalaMacrosParadise cross CrossVersion.full)
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig
  val joda = "joda-time" % "joda-time" % Version.joda
  val jodaConvert = "org.joda" % "joda-convert" % Version.jodaConvert

  // testing libraries
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %%  "scalacheck" % Version.scalaCheck % "test"
  val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % Version.scalaCheckShapeless % "test"
}
