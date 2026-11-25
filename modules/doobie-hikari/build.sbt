import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

val doobieVersion = "1.0.0-RC2"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion % Test
)

developers := List(
  Developer("sideeffffect", "Ondra Pelech", "ondra.pelech@gmail.com", url("https://github.com/sideeffffect"))
)
