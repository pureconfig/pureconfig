import Dependencies.Version._
import Utilities._

name := "pureconfig-cats-effect"

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.4.1"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
