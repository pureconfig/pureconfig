import Dependencies._

name := "pureconfig-core"

// crossScalaVersions ~= { _ :+ "2.13.0-RC1" }

libraryDependencies += typesafeConfig

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
