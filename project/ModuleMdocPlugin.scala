import mdoc.MdocPlugin
import mdoc.MdocPlugin.autoImport._
import sbt._
import sbt.Keys._

/** A plugin that generates a synthetic SBT project for documentation for each module it is enabled on. The generated
  * SBT projects depend on the original project and also have a hardcoded dependency on "generic" in order to provide
  * support for auto-derivation in documentation.
  *
  * This is needed because unlike tut there is no separate classpath configuration for documentation. See
  * https://github.com/scalameta/mdoc/issues/155 for details.
  */
object ModuleMdocPlugin extends AutoPlugin {

  object autoImport {
    val mdocLibraryDependencies = settingKey[Seq[ModuleID]]("Declares managed dependencies for the mdoc project.")
    val mdocScalacOptions = settingKey[Seq[String]]("Options for the Scala compiler in the mdoc project.")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    // format: off
    mdocIn := baseDirectory.value / "docs",
    mdocOut := baseDirectory.value,

    mdocLibraryDependencies := Nil,
    mdocScalacOptions := Nil
    // format: on
  )

  override def derivedProjects(proj: ProjectDefinition[_]): Seq[Project] = {
    val moduleProj = LocalProject(proj.id)
    val docProjId = s"${proj.id}-docs"
    val docProjRoot = proj.base / "target" / "docs-project"

    val docProj =
      Project(docProjId, docProjRoot)
        .enablePlugins(MdocPlugin)
        .dependsOn(moduleProj)
        .dependsOn(LocalProject("generic")) // Allow auto-derivation in documentation
        .settings(
          // format: off
          name := docProjId,

          mdocIn := (mdocIn in moduleProj).value,
          mdocOut := (mdocOut in moduleProj).value,
          mdocExtraArguments += "--no-link-hygiene",
          mdocVariables := Map("VERSION" -> version.value),

          libraryDependencies ++= (mdocLibraryDependencies in moduleProj).value,
          scalacOptions ++= (mdocScalacOptions in moduleProj).value,

          skip in publish := true
          // format: on
        )

    List(docProj)
  }
}
