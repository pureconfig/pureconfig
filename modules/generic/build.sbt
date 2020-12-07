import Dependencies._

name := "pureconfig-generic"

libraryDependencies ++= Seq(
  shapeless,
  scalaCheckShapeless % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.generic.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
