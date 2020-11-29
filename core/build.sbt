import Dependencies.{Version, _}

name := "pureconfig-core"

libraryDependencies += typesafeConfig
crossScalaVersions += Version.scala30

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
