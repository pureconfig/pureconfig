import Dependencies.Version._

name := "pureconfig-spark"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.2.2" % "provided")
mdocLibraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.2.2")
