import Dependencies.Version._

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.20" % "provided",
  "com.typesafe.akka" %% "akka-http" % "10.2.10"
)
mdocLibraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.20"
)

developers := List(
  Developer("himanshu4141", "Himanshu Yadav", "himanshu4141@gmail.com", url("https://github.com/himanshu4141"))
)
