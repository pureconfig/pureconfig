import Dependencies.Version._
import Utilities._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.7",
  "io.circe" %% "circe-literal" % "0.14.7" % Test,
  "org.typelevel" %% "jawn-parser" % "1.5.1" % Test
)

developers := List(
  Developer("moradology", "Nathan Zimmerman", "npzimmerman@gmail.com", url("https://github.com/moradology"))
)
