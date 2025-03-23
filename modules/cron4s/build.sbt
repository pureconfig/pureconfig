import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies += "com.github.alonsodomin.cron4s" %% "cron4s-core" % "0.8.2"

developers := List(
  Developer("bardurdam", "Bárður Viberg Dam", "bardurdam@gmail.com", url("https://github.com/bardurdam"))
)
