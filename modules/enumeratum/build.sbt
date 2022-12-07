import Dependencies.Version._

name := "pureconfig-enumeratum"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("com.beachape" %% "enumeratum" % "1.7.2")

developers := List(Developer("aeons", "Bjørn Madsen", "bm@aeons.dk", url("https://github.com/aeons")))
