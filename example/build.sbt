name := "example"

version := "1.0"

scalaVersion := "2.12.11"

val VersionPattern = """version in ThisBuild := "([^"]*)"""".r
val pureconfigVersion = IO.read(file("../version.sbt")).trim match {
  case VersionPattern(ver) => ver
  case ex =>
    println(s"""'$ex'""")
    throw new Exception("Could not parse PureConfig version")
}

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % pureconfigVersion)

crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.3")

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
