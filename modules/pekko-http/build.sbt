import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.4.0" % "provided",
  "org.apache.pekko" %% "pekko-http" % "1.3.0"
)
mdocLibraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % "1.4.0"
)
