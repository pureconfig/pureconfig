name := "example"

version := "1.0"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.11.0-SNAPSHOT")

crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-RC1")

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
