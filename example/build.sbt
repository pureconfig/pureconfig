name := "example"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.2")

crossScalaVersions := Seq("2.11.12", "2.12.6")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:experimental.macros",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard")
