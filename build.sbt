import scalariform.formatter.preferences._
import ReleaseTransformations._
import microsites._

enablePlugins(CrossPerProjectPlugin)

lazy val core = (project in file("core")).
  enablePlugins(SbtOsgi).
  enablePlugins(TutPlugin).
  settings(commonSettings).
  settings(tutTargetDirectory := (baseDirectory.value) / "..").
  dependsOn(macros).
  dependsOn(macros % "test->test") // provides helpers to test pureconfig macros

lazy val docs = (project in file("docs")).
  enablePlugins(MicrositesPlugin).
  settings(commonSettings, publishArtifact := false).
  settings(micrositesSettings).
  dependsOn(core)

lazy val macros = (project in file("macros")).
  settings(commonSettings)

def module(proj: Project) = proj.
  enablePlugins(SbtOsgi).
  enablePlugins(TutPlugin).
  dependsOn(core).
  dependsOn(core % "test->test"). // In order to reuse the scalacheck generators
  settings(commonSettings)

lazy val akka = module(project) in file("modules/akka")
lazy val cats = module(project) in file("modules/cats")
lazy val enum = module(project) in file("modules/enum")
lazy val enumeratum = module(project) in file("modules/enumeratum")
lazy val javax = module(project) in file("modules/javax")
lazy val joda = module(project) in file("modules/joda")
lazy val scalaxml = module(project) in file("modules/scala-xml")
lazy val squants = module(project) in file("modules/squants")

lazy val commonSettings = Seq(
  organization := "com.github.pureconfig",
  homepage := Some(url("https://github.com/pureconfig/pureconfig")),
  licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/")),

  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3"),

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

  scalacOptions in (Compile, console) ~= (_ filterNot Set("-Xfatal-warnings", "-Ywarn-unused-import").contains),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  // use sbt <module_name>/test:console to run an ammonite console
  libraryDependencies += "com.lihaoyi" % "ammonite" % "1.0.1" % "test" cross CrossVersion.patch,
  initialCommands in (Test, console) := """ammonite.Main().run()""",

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
  micrositeName := "pureconfig",
  micrositeDescription := "A boilerplate-free library for loading configuration files ",
  micrositeAuthor := "pureconfig authors",
  micrositeHomepage := "https://pureconfig.github.io/",
  //micrositeBaseUrl := "pureconfig", // keep this empty to not have a base URL
  //micrositeDocumentationUrl := "api/", // don't have yet
  micrositeGithubOwner := "pureconfig",
  micrositeGithubRepo := "pureconfig",
  micrositeFavicons := Seq(
    MicrositeFavicon("pureconfig-logo-16x16.ico", "16x16"),
    MicrositeFavicon("pureconfig-logo-32x32.ico", "32x32")),
  micrositePalette := Map(
        "brand-primary"     -> "#f62333",
        "brand-secondary"   -> "#8b0000",
        "brand-tertiary"    -> "#320000",
        "gray-dark"         -> "#453E46",
        "gray"              -> "#837F84",
        "gray-light"        -> "#E3E2E3",
        "gray-lighter"      -> "#F4F3F4",
        "white-color"       -> "#FFFFFF"),
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
  "-Ywarn-dead-code")

lazy val scala211LintFlags = allVersionLintFlags ++ Seq(
  "-Ywarn-numeric-widen", // In 2.10 this produces a strange spurious error
  "-Ywarn-unused-import", // Not available in 2.10
  "-Xlint")

lazy val scala212LintFlags = allVersionLintFlags ++ Seq(
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xlint:-unused,_") // Scala 2.12.3 has excessive warnings about unused implicits. See https://github.com/scala/bug/issues/10270

releaseTagComment := s"Release ${(version in ThisBuild).value}"
releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}"

// redefine the release process so that we use sbt-doge cross building operator (+)
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("+test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  releaseStepCommandAndRemaining("sonatypeReleaseAll"))
