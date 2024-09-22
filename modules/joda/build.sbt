import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("joda-time" % "joda-time" % "2.13.0", "org.joda" % "joda-convert" % "2.2.3")

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland"))
)
