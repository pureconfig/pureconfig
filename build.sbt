import scalariform.formatter.preferences._
import ReleaseTransformations._

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
  settings(commonSettings).
  settings(name := "pureconfig", tutTargetDirectory := file(".")).
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
  dependsOn(generic % "tut"). // Allow auto-derivation in documentation
  settings(commonSettings, tutTargetDirectory := baseDirectory.value)

lazy val akka = module(project) in file("modules/akka")
lazy val cats = module(project) in file("modules/cats")
lazy val `cats-effect` = module(project) in file("modules/cats-effect")
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

lazy val commonSettings = Seq(
  organization := "com.github.pureconfig",
  homepage := Some(url("https://github.com/pureconfig/pureconfig")),
  licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/")),

  developers := List(
    Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
    Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland")),
    Developer("jcazevedo", "Joao Azevedo", "joao.c.azevedo@gmail.com", url("https://github.com/jcazevedo")),
    Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")),
    Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr"))),

  scalaVersion := "2.12.7",
  crossScalaVersions := Seq("2.11.12", "2.12.7"),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")),

  crossVersionSharedSources(unmanagedSourceDirectories in Compile),
  crossVersionSharedSources(unmanagedSourceDirectories in Test),

  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => scala212LintFlags
    case Some((2, 11)) => scala211LintFlags
    case _ => allVersionLintFlags
  }),

  scalacOptions in Test += "-Xmacro-settings:materialize-derivations",

  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  scalacOptions in Tut --= Seq("-Ywarn-unused-import", "-Xmacro-settings:materialize-derivations"),

  scalariformPreferences := scalariformPreferences.value
    .setPreference(DanglingCloseParenthesis, Prevent)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(SpacesAroundMultiImports, true),

  initialize := {
    val required = "1.8"
    val current = sys.props("java.specification.version")
    assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
  },

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

// add support for Scala version ranges such as "scala-2.11+" in source folders (single version folders such as
// "scala-2.10" are natively supported by SBT)
def crossVersionSharedSources(unmanagedSrcs: SettingKey[Seq[File]]) = {
  unmanagedSrcs ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, y)) if y >= 11 => unmanagedSrcs.value.map { dir => new File(dir.getPath + "-2.11+") }
    case _ => Nil
  })
}

lazy val allVersionLintFlags = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import")

lazy val scala211LintFlags = allVersionLintFlags ++ Seq(
  "-Xlint")

lazy val scala212LintFlags = allVersionLintFlags ++ Seq(
  "-Xlint:-unused,_") // Scala 2.12.3 has excessive warnings about unused implicits. See https://github.com/scala/bug/issues/10270

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
