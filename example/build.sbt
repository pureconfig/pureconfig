name := "example"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",

  // needed for Scala 2.10
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch))

crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2")

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
