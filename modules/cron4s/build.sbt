import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies += "com.github.alonsodomin.cron4s" %% "cron4s-core" % "0.6.1"

developers := List(
  Developer("bardurdam", "Bárður Viberg Dam", "bardurdam@gmail.com", url("https://github.com/bardurdam"))
)
