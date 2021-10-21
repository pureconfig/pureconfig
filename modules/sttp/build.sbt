import Dependencies.Version._

name := "pureconfig-sttp"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.model" %% "core" % "1.4.16"
)

developers := List(Developer("bszwej", "Bartlomiej Szwej", "bszwej@gmail.com", url("https://github.com/bszwej")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.sttp.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
