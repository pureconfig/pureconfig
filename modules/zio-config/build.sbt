import Dependencies.Version._

name := "pureconfig-zio-config"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-config-typesafe" % "1.0.9"
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.zioconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
