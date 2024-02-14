import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % "1.0.2" % "provided",
  "org.apache.pekko" %% "pekko-http" % "1.0.1"
)
mdocLibraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % "1.0.2"
)

developers := List(
  Developer("himanshu4141", "Himanshu Yadav", "himanshu4141@gmail.com", url("https://github.com/himanshu4141"))
)
