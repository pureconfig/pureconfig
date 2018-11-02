import Dependencies._

name := "pureconfig-macros"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))
