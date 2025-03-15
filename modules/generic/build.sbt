import Dependencies.Version._

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.13",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
