import Dependencies.Version._

name := "pureconfig-akka"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.6.19")

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
