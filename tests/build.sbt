import Dependencies._

name := "pureconfig-tests"

libraryDependencies ++= Seq(scalaCheckShapeless % "test")

skip in publish := true
