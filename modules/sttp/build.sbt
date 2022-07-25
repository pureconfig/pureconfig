import Dependencies.Version._

name := "pureconfig-sttp"

crossScalaVersions := Seq(scala212, scala213, scala31)

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.model" %% "core" % "1.5.0"
)

developers := List(Developer("bszwej", "Bartlomiej Szwej", "bszwej@gmail.com", url("https://github.com/bszwej")))
