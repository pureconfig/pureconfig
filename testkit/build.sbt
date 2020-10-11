import Dependencies._

name := "pureconfig-testkit"

libraryDependencies ++= Seq(scalaTest, scalaCheck, scalaTestPlusScalaCheck)

skip in publish := true
