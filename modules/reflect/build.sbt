import Dependencies._

name := "pureconfig-magnolia"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.11")) }

libraryDependencies ++= Seq(scalaCheckShapeless % "test")

developers := List(
  Developer("chetanmeh", "Chetan Mehrotra", "chetan.mehrotra@gmail.com", url("https://chetanmeh.github.io")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.reflect.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
