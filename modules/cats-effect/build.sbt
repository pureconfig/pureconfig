import Dependencies.Version._
import Utilities._

name := "pureconfig-cats-effect"

crossScalaVersions := Seq(scala212, scala213, scala30, scala31)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.14"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
