import Dependencies._

name := "pureconfig-tests"

crossScalaVersions ~= { _ :+ "2.13.0" }

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck.value,
  scalaCheckShapeless.value % "test")

skip in publish := true
