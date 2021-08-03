import Dependencies.Version._
import Utilities._

name := "pureconfig-scala-xml"

crossScalaVersions := Seq(scala212, scala213)

// Scala 2.12 depends on an old version of scala-xml
libraryDependencies ++= forScalaVersions {
  case (2, 12) => Seq("org.scala-lang.modules" %% "scala-xml" % "1.3.0")
  case _ => Seq("org.scala-lang.modules" %% "scala-xml" % "2.0.1")
}.value

developers := List(Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.scalaxml.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
