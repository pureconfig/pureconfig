import Dependencies.Version._
import Utilities._

name := "pureconfig-cats-effect2"

crossScalaVersions := Seq(scala212, scala213, scala31)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "2.5.5"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
