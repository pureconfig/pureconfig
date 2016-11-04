scalariformSettings

name := "pureconfig"

organization := "com.github.melrief"

version := "0.3.3"

homepage := Some(url("https://github.com/melrief/pureconfig"))

licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/"))

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.5", "2.11.8")

scalacOptions ++= Seq("-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  "com.typesafe" % "config" % "1.3.1"
) ++ testOnlyDeps

val testOnlyDeps = List(
  "org.scalatest" %% "scalatest" % "3.0.0",
  "joda-time" % "joda-time" % "2.9.4",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.scalacheck" %%  "scalacheck" % "1.13.4",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3"
).map(_ % "test")

initialize := {
  val required = "1.8"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
    <scm>
      <url>git@github.com:melrief/pureconfig.git</url>
      <connection>scm:git:git@github.com:melrief/pureconfig.git</connection>
    </scm>
    <developers>
      <developer>
        <id>melrief</id>
        <name>Mario Pastorelli</name>
        <url>https://github.com/melrief</url>
      </developer>
    </developers>)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig", "pureconfig.syntax", "pureconfig.error")

OsgiKeys.privatePackage := Seq()

OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")

val allVersionCompilerLintSwitches = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code"
)

val newerCompilerLintSwitches = Seq(
  "-Ywarn-unused-import", // Not available in 2.10
  "-Ywarn-numeric-widen" // In 2.10 this produces a some strange spurious error
)

scalacOptions ++= allVersionCompilerLintSwitches

scalacOptions in (Compile, console) ~= (_ filterNot (Set("-Xfatal-warnings", "-Ywarn-unused-import").contains))

scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, scalaMajor)) if scalaMajor >= 11 => newerCompilerLintSwitches
}.toList.flatten
