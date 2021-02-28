import Dependencies.Version._
import Utilities._

name := "pureconfig-cats"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.4.2",
  "org.typelevel" %% "cats-laws" % "2.4.2" % "test",
  "org.typelevel" %% "discipline-scalatest" % "2.1.2" % "test"
)

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.cats.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
