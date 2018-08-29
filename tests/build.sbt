import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(
  scalaMacrosParadise,
  scalaTest,
  scalaCheck,
  scalaCheckShapeless.value)

skip in publish := true
