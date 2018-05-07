import Dependencies._

name := "pureconfig-macros"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.typelevel" %% "macro-compat" % "1.1.1",
  shapeless.value % "test",
  scalaMacrosParadise,
  scalaCheck)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))
