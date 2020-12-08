import Dependencies.{Version, _}

name := "pureconfig-core"

crossScalaVersions += Version.scala30

libraryDependencies += typesafeConfig

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
