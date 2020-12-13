import Dependencies.Version._

name := "pureconfig-testkit"

crossScalaVersions := Seq(scala211, scala212, scala213, scala30)

libraryDependencies ++= Seq(
  Dependencies.scalaTest,
  Dependencies.scalaCheck.withDottyCompat(scalaVersion.value),
  Dependencies.scalaTestPlusScalaCheck.intransitive().withDottyCompat(scalaVersion.value)
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

skip in publish := true
