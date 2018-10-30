import Dependencies._

name := "pureconfig-core"

libraryDependencies ++= Seq(
  typesafeConfig)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
