import Dependencies.Version._
import Utilities._

name := "pureconfig-circe"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-literal" % "0.14.1" % Test,
  "org.typelevel" %% "jawn-parser" % "1.2.0" % Test
)

developers := List(
  Developer("moradology", "Nathan Zimmerman", "npzimmerman@gmail.com", url("https://github.com/moradology"))
)
