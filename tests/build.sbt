import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck,
  scalaTestPlusScalaCheck,
  scalaCheckShapeless % "test")

skip in publish := true
