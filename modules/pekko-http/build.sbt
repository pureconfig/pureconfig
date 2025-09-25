import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.2.1" % "provided",
  "org.apache.pekko" %% "pekko-http" % "1.2.0"
)
mdocLibraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.2.1"
)
