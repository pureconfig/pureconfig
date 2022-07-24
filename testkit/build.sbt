import Dependencies.Version._

name := "pureconfig-testkit"

crossScalaVersions := Seq(scala212, scala213, scala30, scala31)

libraryDependencies ++= Seq(
  Dependencies.scalaTest.value,
  Dependencies.scalaCheck.cross(CrossVersion.for3Use2_13),
  // We need to ignore the transitive dependencies to avoid having scalatest-core and scalactic with conflicting
  // cross-version suffixes, since we'd get the native 3.x version from the `scalaTest` dependency and the 2.x
  // dependency from `scalaTestPlusScalaCheck` with the `cross(CrossVersion.for3Use2_13)` flag.
  Dependencies.scalaTestPlusScalaCheck.value.intransitive().cross(CrossVersion.for3Use2_13)
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

publish / skip := true
