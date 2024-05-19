import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.12",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
