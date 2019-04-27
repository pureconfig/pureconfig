import Dependencies._

name := "pureconfig-macros"

crossScalaVersions ~= { _ :+ "2.13.0-RC1" }

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))
