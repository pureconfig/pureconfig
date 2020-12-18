import Dependencies.Version._

name := "pureconfig-generic"

crossScalaVersions := Seq(scala211, scala212, scala213)

libraryDependencies ++= Seq(
  Dependencies.shapeless,
  Dependencies.scalaCheckShapeless % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.generic.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
