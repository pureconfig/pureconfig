import Dependencies.Version._
import Utilities._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.13",
  "io.circe" %% "circe-literal" % "0.14.13" % Test,
  "org.typelevel" %% "jawn-parser" % "1.6.0" % Test
)

developers := List(
  Developer("moradology", "Nathan Zimmerman", "npzimmerman@gmail.com", url("https://github.com/moradology"))
)
