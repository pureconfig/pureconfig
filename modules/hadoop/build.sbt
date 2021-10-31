import Dependencies.Version._

name := "pureconfig-hadoop"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("org.apache.hadoop" % "hadoop-common" % "3.3.1" % "provided")
mdocLibraryDependencies ++= Seq("org.apache.hadoop" % "hadoop-common" % "3.3.1")

developers := List(Developer("lmnet", "Yuriy Badalyantc", "lmnet89@gmail.com", url("https://github.com/lmnet")))
