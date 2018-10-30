name := "example"

version := "1.0"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.10.0")

crossScalaVersions := Seq("2.11.12", "2.12.7")

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
