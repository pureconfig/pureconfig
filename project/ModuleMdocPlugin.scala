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

  override def derivedProjects(proj: ProjectDefinition[_]): Seq[Project] = {
    val docProjId = s"${proj.id}-docs"
    val docProjRoot = proj.base / "target" / "docs-project"
    val docProj =
      Project(docProjId, docProjRoot)
        .enablePlugins(MdocPlugin)
        .dependsOn(LocalProject(proj.id))
        .dependsOn(LocalProject("generic")) // Allow auto-derivation in documentation
        .settings(
          name := docProjId,
          mdocIn := proj.base / "docs",
          mdocOut := proj.base,
          mdocExtraArguments += "--no-link-hygiene",
          mdocVariables := Map("VERSION" -> version.value),
          skip in publish := true
        )
    List(docProj)
  }
}
