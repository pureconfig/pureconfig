import Dependencies._

name := "pureconfig-tests"

crossScalaVersions ~= { _ :+ "2.13.0" }

libraryDependencies ++= Seq(
  scalaTest.withConfigurations(Some("compile")),
  scalaCheck.value.withConfigurations(Some("compile")),
  scalaCheckShapeless.value.withConfigurations(Some("compile")))

skip in publish := true
