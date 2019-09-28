import Utilities._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import scalariform.formatter.preferences._

lazy val core = (project in file("core")).
  enablePlugins(BoilerplatePlugin, SbtOsgi, TutPlugin).
  settings(commonSettings).
  dependsOn(macros)

// A special module for now, since `tests` depend on it. We should improve this organization later by separating the
// test helpers (which all projects' tests should depend on) from the core+generic test implementations.
lazy val generic = (project in file("modules/generic")).
  enablePlugins(SbtOsgi, TutPlugin).
  dependsOn(core).
  settings(commonSettings, tutTargetDirectory := baseDirectory.value)

lazy val macros = (project in file("macros")).
  enablePlugins(TutPlugin).
  settings(commonSettings)

lazy val tests = (project in file("tests")).
  enablePlugins(BoilerplatePlugin).
  settings(commonSettings).
  dependsOn(core, generic).
  dependsOn(macros % "test->test") // provides helpers to test pureconfig macros

// aggregates pureconfig-core and pureconfig-generic with the original "pureconfig" name
lazy val bundle = (project in file("bundle")).
  enablePlugins(SbtOsgi, TutPlugin).
  settings(commonSettings, tutTargetDirectory := file(".")).
  dependsOn(core, generic)

lazy val docs = (project in file("docs")).
  enablePlugins(MicrositesPlugin).
  settings(commonSettings, publishArtifact := false).
  settings(micrositesSettings).
  dependsOn(bundle)

def module(proj: Project) = proj.
  enablePlugins(SbtOsgi, TutPlugin).
  dependsOn(core).
  dependsOn(tests % "test->test"). // In order to reuse thDerivationSuite scalacheck generators
  dependsOn(generic % "Tut"). // Allow auto-derivation in documentation
  settings(commonSettings, tutTargetDirectory := baseDirectory.value)

lazy val akka = module(project) in file("modules/akka")
lazy val cats = module(project) in file("modules/cats")
lazy val `cats-effect` = module(project) in file("modules/cats-effect")
lazy val circe = module(project) in file("modules/circe")
lazy val cron4s = module(project) in file("modules/cron4s")
lazy val enum = module(project) in file("modules/enum")
lazy val enumeratum = module(project) in file("modules/enumeratum")
lazy val fs2 = module(project) in file("modules/fs2")
lazy val hadoop = module(project) in file("modules/hadoop")
lazy val http4s = module(project) in file("modules/http4s")
lazy val javax = module(project) in file("modules/javax")
lazy val joda = module(project) in file("modules/joda")
lazy val `scala-xml` = module(project) in file("modules/scala-xml")
lazy val scalaz = module(project) in file("modules/scalaz")
lazy val squants = module(project) in file("modules/squants")
lazy val sttp = module(project) in file("modules/sttp")
lazy val yaml = module(project) in file("modules/yaml")

lazy val commonScalaVersionSettings = Seq(
  crossScalaVersions := Seq("2.12.10", "2.13.0", "2.11.12"),
  scalaVersion := crossScalaVersions.value.head)

lazy val commonSettings = commonScalaVersionSettings ++ Seq(
  organization := "com.github.pureconfig",
  homepage := Some(url("https://github.com/pureconfig/pureconfig")),
  licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/")),

  developers := List(
    Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
    Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland")),
    Developer("jcazevedo", "Joao Azevedo", "joao.c.azevedo@gmail.com", url("https://github.com/jcazevedo")),
    Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")),
    Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr"))),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")),

  crossVersionSharedSources(unmanagedSourceDirectories in Compile),
  crossVersionSharedSources(unmanagedSourceDirectories in Test),

  scalacOptions ++= lintFlags.value,

  scalacOptions in Test ~= { _.filterNot(_.contains("-Ywarn-unused")) },
  scalacOptions in Test += "-Xmacro-settings:materialize-derivations",

  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  scalacOptions in Tut --= Seq("-Ywarn-unused-import", "-Xmacro-settings:materialize-derivations"),

  scalariformPreferences := scalariformPreferences.value
    .setPreference(DanglingCloseParenthesis, Prevent)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(SpacesAroundMultiImports, true),

  autoAPIMappings := true,

  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  })

lazy val micrositesSettings = Seq(
  micrositeName := "PureConfig",
  micrositeDescription := "A boilerplate-free library for loading configuration files",
  micrositeAuthor := "com.github.pureconfig",
  micrositeHomepage := "https://pureconfig.github.io/",
  //micrositeBaseUrl := "pureconfig", // keep this empty to not have a base URL
  micrositeDocumentationUrl := "docs/",
  micrositeGithubOwner := "pureconfig",
  micrositeGithubRepo := "pureconfig",
  micrositePalette := Map(
        "brand-primary"   /* link color       */  -> "#ab4b4b",
        "brand-secondary" /* nav/sidebar back */  -> "#4b4b4b",
        "brand-tertiary"  /* sidebar top back */  -> "#292929",
        "gray-dark"       /* section title    */  -> "#453E46",
        "gray"            /* text color       */  -> "#837F84",
        "gray-light"      /* star back        */  -> "#E3E2E3",
        "gray-lighter"    /* code back        */  -> "#F4F3F4",
        "white-color"                             -> "#FFFFFF"),
  micrositeGitterChannel := false // ugly
)

// add support for Scala version ranges such as "scala-2.12+" in source folders (single version folders such as
// "scala-2.11" are natively supported by SBT).
// In order to keep this simple, we're doing this case by case, taking advantage of the fact that we intend to support
// only 3 major versions at any given moment.
def crossVersionSharedSources(unmanagedSrcs: SettingKey[Seq[File]]) = {
  unmanagedSrcs ++= {
    val minor = CrossVersion.partialVersion(scalaVersion.value).map(_._2)
    List(
      if(minor.exists(_ <= 12)) unmanagedSrcs.value.map { dir => new File(dir.getPath + "-2.12-") } else Nil,
      if(minor.exists(_ >= 12)) unmanagedSrcs.value.map { dir => new File(dir.getPath + "-2.12+") } else Nil,
    ).flatten
  }
}

lazy val lintFlags = {
  lazy val allVersionLintFlags = List(
    "-encoding", "UTF-8", // yes, this is 2 args
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen")

  def withCommon(flags: String*) =
    allVersionLintFlags ++ flags

  forScalaVersions {
    case (2, 11) =>
      withCommon(
        "-deprecation",
        "-Xlint",
        "-Xfatal-warnings",
        "-Yno-adapted-args",
        "-Ywarn-unused-import")

    case (2, 12) =>
      withCommon(
        "-deprecation",                // Either#right is deprecated on Scala 2.13
        "-Xlint:_,-unused",
        "-Xfatal-warnings",
        "-Yno-adapted-args",
        "-Ywarn-unused:_,-implicits")  // Some implicits are intentionally used just as evidences, triggering warnings

    case (2, 13) =>
      withCommon(
        "-Ywarn-unused:_,-implicits")

    case _ =>
      withCommon()
  }
}

// Use common settings for Scala versions in the root project
commonScalaVersionSettings

// do not publish the root project
skip in publish := true

releaseCrossBuild := true
releaseTagComment := s"Release ${(version in ThisBuild).value}"
releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}"

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
  setNextVersion,
  commitNextVersion,
  pushChanges,
  releaseStepCommandAndRemaining("sonatypeReleaseAll"))
