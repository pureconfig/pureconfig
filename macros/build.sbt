import Dependencies._

name := "pureconfig-macros"

crossScalaVersions ~= { _ :+ "2.13.0" }

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)

// Scala has excessive warnings about unused implicits on macro exps (https://github.com/scala/bug/issues/10270)
scalacOptions ~= { _.filterNot(_.contains("-Ywarn-unused")) }

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))
