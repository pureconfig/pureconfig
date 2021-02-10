import Dependencies.Version._
import Utilities._

name := "pureconfig-cats"

crossScalaVersions := Seq(scala211, scala212, scala213)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % forScalaVersions { case (2, 11) => "2.0.0"; case _ => "2.4.1" }.value,
  "org.typelevel" %% "cats-laws" % forScalaVersions { case (2, 11) => "2.0.0"; case _ => "2.4.1" }.value % "test",
  "org.typelevel" %% "discipline-scalatest" % "2.1.1" % "test"
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
