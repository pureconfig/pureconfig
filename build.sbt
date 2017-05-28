import scalariform.formatter.preferences._
import ReleaseTransformations._

enablePlugins(CrossPerProjectPlugin)

lazy val core = (project in file("core")).
  settings(commonSettings,
    tutTargetDirectory := file(".")
  )

lazy val docs = (project in file("docs")).
  settings(commonSettings, publishArtifact := false).
  dependsOn(core)

def module(proj: Project) = proj.
  dependsOn(core).
  dependsOn(core % "test->test"). // In order to reuse the scalacheck generators
  settings(commonSettings)

lazy val cats = module(project) in file("modules/cats")
lazy val enumeratum = module(project) in file("modules/enumeratum")
lazy val enum = module(project) in file("modules/enum")
lazy val joda = module(project) in file("modules/joda")
lazy val scalaxml = module(project) in file("modules/scala-xml")
lazy val squants = module(project) in file("modules/squants")
lazy val javax = module(project) in file("modules/javax")

// akka 2.4 isn't published for Scala 2.10
lazy val akka = (module(project) in file("modules/akka")).
  settings(crossScalaVersions ~= { oldVersions => oldVersions.filterNot(_.startsWith("2.10")) })

lazy val commonSettings = tutSettings ++ Seq(
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2"),

  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => scala212LintFlags
    case Some((2, 11)) => scala211LintFlags
    case _ => allVersionLintFlags
  }),

  scalacOptions in (Compile, console) ~= (_ filterNot Set("-Xfatal-warnings", "-Ywarn-unused-import").contains),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  // use sbt <module_name>/test:console to run an ammonite console
  libraryDependencies += "com.lihaoyi" % "ammonite" % "0.9.0" % "test" cross CrossVersion.patch,
  initialCommands in (Test, console) := """ammonite.Main().run()""",

  scalariformPreferences := scalariformPreferences.value
    .setPreference(DanglingCloseParenthesis, Prevent)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(SpacesAroundMultiImports, true),

  initialize := {
    val required = "1.8"
    val current = sys.props("java.specification.version")
    assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
  },

  tutTargetDirectory := baseDirectory.value,
  autoAPIMappings := true)

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
  "-Xlint:-unused,_", // Scala 2.12.2 has excessive warnings about unused implicits. See https://github.com/scala/bug/issues/10270
  "-Ywarn-unused:-params")

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
