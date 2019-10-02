import Dependencies._

name := "pureconfig-magnolia"

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.11.0",
  scalaCheckShapeless % "test")

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.magnolia.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
