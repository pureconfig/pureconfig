import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck,
  scalaCheckShapeless.value)

skip in publish := true
