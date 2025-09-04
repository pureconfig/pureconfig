import Dependencies.Version._
import Utilities._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-xml" % "2.4.0")

developers := List(Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")))
