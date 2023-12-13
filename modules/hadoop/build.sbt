import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("org.apache.hadoop" % "hadoop-common" % "3.3.6" % "provided")
mdocLibraryDependencies ++= Seq("org.apache.hadoop" % "hadoop-common" % "3.3.6")

developers := List(Developer("lmnet", "Yuriy Badalyantc", "lmnet89@gmail.com", url("https://github.com/lmnet")))
