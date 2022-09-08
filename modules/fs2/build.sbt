import Dependencies.Version._

name := "pureconfig-fs2"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "3.2.14",
  "co.fs2" %% "fs2-io" % "3.2.14"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
