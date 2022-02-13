import Dependencies.Version._

name := "pureconfig-generic"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.8",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
