import Dependencies.Version._

name := "pureconfig-fs2"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "3.0.2",
  "co.fs2" %% "fs2-io" % "3.0.2"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.fs2.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
