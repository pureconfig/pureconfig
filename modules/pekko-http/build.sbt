import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "2.0.0" % "provided",
  "org.apache.pekko" %% "pekko-http" % "2.0.0"
)
mdocLibraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "2.0.0"
)
