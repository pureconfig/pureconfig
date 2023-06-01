import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.3",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  // We're using shapeless for illTyped in tests.
  "com.chuusai" %% "shapeless" % "2.3.10" % Test
)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
