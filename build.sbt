import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

lazy val core = (project in file("core")).
  settings(settings)

lazy val enumeratum = (project in file("modules/enumeratum")).
  settings(settings).
  dependsOn(core)

lazy val joda = (project in file("modules/joda")).
  settings(settings).
  dependsOn(core).
  dependsOn(core % "test->test") // In order to reuse the date/time related scalacheck generators.

lazy val allVersionCompilerLintSwitches = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code"
)

lazy val newerCompilerLintSwitches = Seq(
  "-Ywarn-unused-import", // Not available in 2.10
  "-Ywarn-numeric-widen" // In 2.10 this produces a some strange spurious error
)

lazy val formattingPreferences = FormattingPreferences()
  .setPreference(DanglingCloseParenthesis, Prevent)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(SpacesAroundMultiImports, true)

lazy val formattingSettings = SbtScalariform.scalariformSettings ++ Seq(
  ScalariformKeys.preferences in Compile := formattingPreferences,
  ScalariformKeys.preferences in Test := formattingPreferences)

lazy val settings = Seq(
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.10.5", "2.11.8", "2.12.1"),
  scalacOptions ++= allVersionCompilerLintSwitches,
  scalacOptions in (Compile, console) ~= (_ filterNot (Set("-Xfatal-warnings", "-Ywarn-unused-import").contains)),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
    case Some((2, scalaMajor)) if scalaMajor >= 11 => newerCompilerLintSwitches
  }.toList.flatten,
  initialize := {
    val required = "1.8"
    val current  = sys.props("java.specification.version")
    assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
  }) ++ formattingSettings
