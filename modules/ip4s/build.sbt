import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

val ip4sVersion = "3.7.0"

libraryDependencies ++= Seq(
  "com.comcast" %% "ip4s-core" % ip4sVersion,
  "com.comcast" %% "ip4s-test-kit" % ip4sVersion % Test
)

developers := List(
  Developer("geirolz", "David Geirola", "david.geirolz@gmail.com", url("https://github.com/geirolz"))
)
