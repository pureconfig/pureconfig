import Dependencies.Version._

name := "pureconfig-generic"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.7",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.generic.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
