import Dependencies.Version._
import Utilities._

name := "pureconfig-cats"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.typelevel" %% "cats-laws" % "2.8.0" % "test",
  "org.typelevel" %% "discipline-scalatest" % "2.1.5" % "test"
)

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
