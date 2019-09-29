import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck,
  scalaCheckShapeless % "test")

skip in publish := true
