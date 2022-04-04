import Dependencies.Version._
import Utilities._

name := "pureconfig-scala-xml"

crossScalaVersions := Seq(scala212, scala213)

// Scala 2.12 depends on an old version of scala-xml
libraryDependencies ++= forScalaVersions {
  case (2, 12) => Seq("org.scala-lang.modules" %% "scala-xml" % "2.1.0")
  case _ => Seq("org.scala-lang.modules" %% "scala-xml" % "2.0.1")
}.value

developers := List(Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")))
