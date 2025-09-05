import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "3.12.2",
  "co.fs2" %% "fs2-io" % "3.12.2"
)

developers := List(Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))
