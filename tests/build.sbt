import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck,
  scalaCheckShapeless)

skip in publish := true
