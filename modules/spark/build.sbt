import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.5.5" % "provided")
mdocLibraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.5.5")
