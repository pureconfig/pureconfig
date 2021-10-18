import Dependencies.Version._

name := "pureconfig-core"

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies += Dependencies.typesafeConfig

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
