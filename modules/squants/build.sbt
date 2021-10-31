import Dependencies.Version._

name := "pureconfig-squants"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("org.typelevel" %% "squants" % "1.8.3")

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("cranst0n", "Ian McIntosh", "cranston.ian@gmail.com", url("https://github.com/cranst0n"))
)
