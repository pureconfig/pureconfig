import Dependencies.{Version, _}

name := "pureconfig-testkit"

crossScalaVersions += Version.scala30

libraryDependencies ++= Seq(scalaTest, scalaCheck.withDottyCompat(scalaVersion.value), scalaTestPlusScalaCheck.intransitive().withDottyCompat(scalaVersion.value))

skip in publish := true
