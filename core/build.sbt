import Dependencies.Version._

name := "pureconfig-core"

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies += "com.typesafe" % "config" % "1.4.2"
