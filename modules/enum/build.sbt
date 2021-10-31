import Dependencies.Version._

name := "pureconfig-enum"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("org.julienrf" %% "enum" % "3.1")

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland"))
)
