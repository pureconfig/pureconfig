import Dependencies.Version._

name := "pureconfig-akka-http"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.19" % "provided",
  "com.typesafe.akka" %% "akka-http" % "10.2.9"
)
mdocLibraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.19"
)

developers := List(
  Developer("himanshu4141", "Himanshu Yadav", "himanshu4141@gmail.com", url("https://github.com/himanshu4141"))
)
