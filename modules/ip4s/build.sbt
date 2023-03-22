import Dependencies.Version._

name := "pureconfig-ip4s"

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("com.comcast" %% "ip4s-core" % "3.3.0")

developers := List(
  Developer("geirolz", "David Geirola", "david.geirolz@gmail.com", url("https://github.com/geirolz"))
)
