import Dependencies.Version._

name := "pureconfig-tests"

crossScalaVersions := Seq(scala211, scala212, scala213, scala30)

skip in publish := true
