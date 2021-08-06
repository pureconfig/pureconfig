import Dependencies.Version._
import Utilities._

name := "pureconfig-cats-effect"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.2.2"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.catseffect.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
