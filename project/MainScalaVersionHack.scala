import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._
import sbt._
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixOnCompile

/** A hack needed to fix PureConfig modules that don't support the main Scala version defined in the root project (e.g.
  * a project using a Scala 3 specific dependency when the main Scala version is 2.x). See
  * [[https://github.com/pureconfig/pureconfig/issues/1082]] for more context.
  *
  * In order to prevent projects like these from breaking builds, the settings defined here effectively generate a dummy
  * project with no source files nor library dependencies. This will ensure SBT doesn't try to fetch non-existent
  * dependencies and compile module's code on an unsupported Scala version.
  *
  * `projectSettings` should be included as the last statement in a module's `build.sbt`. Modules using these settings
  * do not need to define `scalaVersion`. `crossScalaVersions` should include only the Scala versions actually supported
  * by the project.
  */
object MainScalaVersionHack {
  val isDummyProject = settingKey[Boolean]("Whether this is a dummy project generated by MainScalaVersionHack.scala")

  def projectSettings: Seq[Setting[_]] = Seq(
    isDummyProject := scalaVersion.value == (LocalRootProject / scalaVersion).value,

    // Clear sources and library dependencies on dummy projects
    (Compile / sources) := {
      if (isDummyProject.value) Nil else (Compile / sources).value
    },
    (Test / sources) := {
      if (isDummyProject.value) Nil else (Test / sources).value
    },
    libraryDependencies := {
      if (isDummyProject.value) Nil else libraryDependencies.value
    },

    // Disable scalafmt and scalafix
    scalafmtOnCompile := !isDummyProject.value && scalafmtOnCompile.value,
    scalafixOnCompile := !isDummyProject.value && scalafixOnCompile.value,

    // on sbt publish we use +publishSigned so this is probably not necessary, but we can disable it anyway to prevent
    // accidental publishing.
    publish / skip := !isDummyProject.value && (publish / skip).value
  )
}
