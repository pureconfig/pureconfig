import Dependencies.Version._

name := "pureconfig-joda"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("joda-time" % "joda-time" % "2.10.14", "org.joda" % "joda-convert" % "2.2.2")

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland"))
)
