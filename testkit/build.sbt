import Dependencies.Version._

name := "pureconfig-testkit"

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  Dependencies.scalaTest,
  Dependencies.scalaCheck,
  Dependencies.scalaTestPlusScalaCheck
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

publish / skip := true
