import Dependencies.Version._

libraryDependencies ++= Seq(
  "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.10",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  // We're using shapeless for illTyped in tests.
  "com.chuusai" %% "shapeless" % "2.3.13" % Test
)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
