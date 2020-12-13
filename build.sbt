import Dependencies.Version._
import Utilities._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

organization in ThisBuild := "com.github.pureconfig"

lazy val core = (project in file("core"))
  .enablePlugins(BoilerplatePlugin, SbtOsgi)
  .settings(commonSettings)
  .dependsOn(macros)

lazy val macros = (project in file("macros"))
  .settings(commonSettings)

lazy val testkit = (project in file("testkit"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val tests = (project in file("tests"))
  .enablePlugins(BoilerplatePlugin)
  .settings(commonSettings)
  .dependsOn(core, testkit)

// aggregates pureconfig-core and pureconfig-generic with the original "pureconfig" name
lazy val bundle = (project in file("bundle"))
  .enablePlugins(SbtOsgi, ModuleMdocPlugin)
  .settings(commonSettings, mdocOut := file("."))
  .dependsOn(core, generic)

lazy val docs = (project in file("docs"))
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings)
  .dependsOn(bundle)

def genericModule(proj: Project) = proj
  .enablePlugins(SbtOsgi)
  .dependsOn(core)
  .dependsOn(testkit % "test")
  .settings(commonSettings)

def module(proj: Project) = genericModule(proj)
  .enablePlugins(ModuleMdocPlugin)
  .dependsOn(generic % "test")

lazy val akka = module(project) in file("modules/akka")
lazy val `akka-http` = module(project) in file("modules/akka-http")
lazy val cats = module(project) in file("modules/cats")
lazy val `cats-effect` = module(project) in file("modules/cats-effect")
lazy val circe = module(project) in file("modules/circe")
lazy val cron4s = module(project) in file("modules/cron4s")
lazy val enum = module(project) in file("modules/enum")
lazy val enumeratum = module(project) in file("modules/enumeratum")
lazy val fs2 = module(project) in file("modules/fs2")
lazy val generic = genericModule(project) in file("modules/generic") dependsOn `generic-base`
lazy val `generic-base` = genericModule(project) in file("modules/generic-base")
lazy val hadoop = module(project) in file("modules/hadoop")
lazy val http4s = module(project) in file("modules/http4s")
lazy val javax = module(project) in file("modules/javax")
lazy val joda = module(project) in file("modules/joda")
lazy val magnolia = module(project) in file("modules/magnolia") dependsOn `generic-base`
lazy val `scala-xml` = module(project) in file("modules/scala-xml")
lazy val scalaz = module(project) in file("modules/scalaz")
lazy val squants = module(project) in file("modules/squants")
lazy val sttp = module(project) in file("modules/sttp")
lazy val yaml = module(project) in file("modules/yaml")

lazy val commonSettings = Seq(
  // format: off
  homepage := Some(url("https://github.com/pureconfig/pureconfig")),
  licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/")),

  developers := List(
    Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
    Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland")),
    Developer("jcazevedo", "Joao Azevedo", "joao.c.azevedo@gmail.com", url("https://github.com/jcazevedo")),
    Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")),
    Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr"))
  ),

  scalaVersion := scala212,

  resolvers ++= Seq(Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots")),

  crossVersionSharedSources(unmanagedSourceDirectories in Compile),
  crossVersionSharedSources(unmanagedSourceDirectories in Test),

  scalacOptions ++= lintFlags.value,

  scalacOptions in Test ~= { _.filterNot(_.contains("-Ywarn-unused")) },
  scalacOptions in Test ++= forScalaVersions {
    case (2, _) => List("-Xmacro-settings:materialize-derivations")
    case _ => Nil
  }.value,

  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import", "-Ywarn-unused:_,-implicits"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  scalafmtOnCompile := true,

  autoAPIMappings := true,

  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := sonatypePublishToBundle.value
  // format: on
)

// add support for Scala version ranges such as "scala-2.12+" or "scala-2.13-" in source folders (single version folders
// such as "scala-2.11" are natively supported by SBT).
def crossVersionSharedSources(unmanagedSrcs: SettingKey[Seq[File]]) = {
  unmanagedSrcs ++= {
    val versionNumber = CrossVersion.partialVersion(scalaVersion.value)
    val expectedVersions = Seq(scala211, scala212, scala213, scala30).flatMap(CrossVersion.partialVersion)
    expectedVersions.flatMap { case v @ (major, minor) =>
      List(
        if (versionNumber.exists(Ordering[(Long, Long)].lteq(_, v)))
          unmanagedSrcs.value.map { dir => new File(dir.getPath + s"-$major.$minor-") }
        else
          Nil,
        if (versionNumber.exists(Ordering[(Long, Long)].gteq(_, v)))
          unmanagedSrcs.value.map { dir => new File(dir.getPath + s"-$major.$minor+") }
        else
          Nil
      )
    }.flatten
  }
}

lazy val lintFlags = forScalaVersions { v =>
  (v: @unchecked) match {
    case (2, 11) =>
      List(
        "-encoding",
        "UTF-8", // arg for -encoding
        "-feature",
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-Xfatal-warnings",
        "-Yno-adapted-args",
        "-Ywarn-unused-import",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen"
      )

    case (2, 12) =>
      List(
        "-encoding",
        "UTF-8", // arg for -encoding
        "-feature",
        "-unchecked",
        "-deprecation", // Either#right is deprecated on Scala 2.13
        "-Xlint:_,-unused",
        "-Xfatal-warnings",
        "-Yno-adapted-args",
        "-Ywarn-unused:_,-implicits", // Some implicits are intentionally used just as evidences, triggering warnings
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen"
      )

    case (2, 13) =>
      List(
        "-encoding",
        "UTF-8", // arg for -encoding
        "-feature",
        "-unchecked",
        "-Ywarn-unused:_,-implicits",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen"
      )

    case (3, 0) =>
      List(
        "-encoding",
        "UTF-8", // arg for -encoding
        "-feature",
        "-unchecked"
      )
  }
}

// Use the same Scala 2.12 version in the root project as in subprojects
scalaVersion := scala212

// do not publish the root project
skip in publish := true

releaseCrossBuild := true
releaseTagComment := s"Release ${(version in ThisBuild).value}"
releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}"
releaseNextCommitMessage := s"Set version to ${(version in ThisBuild).value}"

// redefine the release process due to https://github.com/sbt/sbt-release/issues/184
// and to append `sonatypeReleaseAll`
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
