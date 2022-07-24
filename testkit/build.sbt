import Dependencies.Version._

name := "pureconfig-testkit"

crossScalaVersions := Seq(scala212, scala213, scala31)

libraryDependencies ++= Seq(
  Dependencies.scalaTest.value,
  Dependencies.scalaCheck,
  Dependencies.scalaTestPlusScalaCheck.value
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

publish / skip := true
