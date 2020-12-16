import Dependencies.Version._

name := "pureconfig-testkit"

crossScalaVersions := Seq(scala211, scala212, scala213, scala30)

libraryDependencies ++= Seq(
  Dependencies.scalaTest,
  Dependencies.scalaCheck.withDottyCompat(scalaVersion.value),
  // We need to ignore the transitive dependencies to avoid having scalatest-core and scalactic with conflicting
  // cross-version suffixes, since we'd get the native 3.x version from the `scalaTest` dependency and the 2.x
  // dependency from `scalaTestPlusScalaCheck` with the `withDottyCompat` flag.
  Dependencies.scalaTestPlusScalaCheck.intransitive().withDottyCompat(scalaVersion.value)
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

skip in publish := true
