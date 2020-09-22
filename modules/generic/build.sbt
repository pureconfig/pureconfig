import Dependencies._

name := "pureconfig-generic"

libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided, shapeless)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.generic.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
