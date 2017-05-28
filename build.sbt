import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import ReleaseTransformations._

enablePlugins(CrossPerProjectPlugin)

lazy val core = (project in file("core")).
  settings(settings,
    tutTargetDirectory := file(".")
  )

lazy val docs = (project in file("docs")).
  settings(settings, publishArtifact := false).
  dependsOn(core)

def module(proj: Project) = proj.
  dependsOn(core).
  dependsOn(core % "test->test"). // In order to reuse the scalacheck generators
  settings(settings)

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

lazy val allVersionCompilerLintSwitches = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code"
)

lazy val scala211Flags = Seq(
  "-Ywarn-unused-import", // Not available in 2.10
  "-Ywarn-numeric-widen" // In 2.10 this produces a strange spurious error
)

// Scala 2.12.2 has excessive warnings about unused implicits. See https://github.com/scala/bug/issues/10270
lazy val scala212Flags = Seq(
  "-Xlint:-unused,_",
  "-Ywarn-unused:-params"
)

lazy val xlint = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => scala212Flags
      case _ => Seq("-Xlint")
    }
  }
)

lazy val formattingPreferences = FormattingPreferences()
  .setPreference(DanglingCloseParenthesis, Prevent)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(SpacesAroundMultiImports, true)

lazy val formattingSettings = SbtScalariform.scalariformSettings ++ Seq(
  ScalariformKeys.preferences in Compile := formattingPreferences,
  ScalariformKeys.preferences in Test := formattingPreferences)

lazy val settings = Seq(
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2"),
  scalacOptions ++= allVersionCompilerLintSwitches,
  scalacOptions in (Compile, console) ~= (_ filterNot (Set("-Xfatal-warnings", "-Ywarn-unused-import").contains)),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, scalaMajor)) if scalaMajor >= 11 => scala211Flags
  }.toList.flatten,
  // use sbt <module_name>/test:console to run an ammonite console
  libraryDependencies += "com.lihaoyi" % "ammonite" % "0.9.0" % "test" cross CrossVersion.patch,
  initialCommands in (Test, console) := """ammonite.Main().run()""",
  initialize := {
    val required = "1.8"
    val current  = sys.props("java.specification.version")
    assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
  },
  autoAPIMappings := true) ++ xlint ++ formattingSettings ++ tutSettings ++ Seq(tutTargetDirectory := baseDirectory.value)

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
