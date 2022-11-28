import Dependencies.Version._

name := "pureconfig-scalaz"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.3.7",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.3.7" % "test"
)

mdocScalacOptions += "-Ypartial-unification"

developers := List(
  Developer("ChernikovP", "Pavel Chernikov", "chernikov.pavel92@gmail.com", url("https://github.com/ChernikovP"))
)
