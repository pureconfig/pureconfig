import Dependencies.Version._
import Utilities._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.6.2"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
