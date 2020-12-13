import Dependencies.Version._

name := "pureconfig-magnolia"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.17.0",
  Dependencies.scalaCheckShapeless % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.magnolia.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
