name := "pureconfig-scala-xml"

libraryDependencies ++= Seq(
  Dependencies.scalaMacrosParadise)

// on scala 2.11+, pull in newer scala-xml library
libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.6")
    case _ =>
      Nil
  }
}

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.scalaxml.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
