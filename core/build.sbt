import Dependencies.Version._

name := "pureconfig-core"

crossScalaVersions := Seq(scala212, scala213, scala31)

libraryDependencies += Dependencies.typesafeConfig
