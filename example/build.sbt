name := "example"

version := "1.0"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.11.2-SNAPSHOT")

crossScalaVersions := Seq("2.13.0", "2.12.9", "2.11.12")

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
