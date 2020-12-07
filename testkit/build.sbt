import Dependencies.{Version, _}

name := "pureconfig-testkit"

crossScalaVersions += Version.scala30

libraryDependencies ++= Seq(
  scalaTest,
  scalaCheck.withDottyCompat(scalaVersion.value),
  scalaTestPlusScalaCheck.intransitive().withDottyCompat(scalaVersion.value)
)

// This is to avoid a warning due to the intransitive dependency of scalaTestPlusScalaCheck.
publishMavenStyle := false

skip in publish := true
