import Dependencies.Version._
import Utilities._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-laws" % "2.10.0" % "test",
  "org.typelevel" %% "discipline-scalatest" % "2.3.0" % "test"
)

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
