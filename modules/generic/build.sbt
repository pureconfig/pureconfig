import Dependencies.Version._
import CrossSettings._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= crossSettings(
  scalaVersion.value,
  if3 = Nil,
  if2 = List(
    "com.chuusai" %% "shapeless" % "2.3.10",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
  )
)
