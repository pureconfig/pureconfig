import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.0.3" % "provided",
  "org.apache.pekko" %% "pekko-http" % "1.0.1"
)
mdocLibraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.0.3"
)
