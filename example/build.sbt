name := "example"

version := "1.0"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.12.1-SNAPSHOT")

crossScalaVersions := Seq("2.12.9", "2.11.12", "2.13.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:experimental.macros",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard")
