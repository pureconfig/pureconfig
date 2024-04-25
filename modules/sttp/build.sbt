import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.model" %% "core" % "1.7.10"
)

developers := List(Developer("bszwej", "Bartlomiej Szwej", "bszwej@gmail.com", url("https://github.com/bszwej")))
