name := "pureconfig-circe"

def onScala211(onScala211: String, onOthers: String) = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => onScala211
    case _ => onOthers
  }
}

libraryDependencies ++= Seq(
  "io.circe"      %% "circe-core"     % onScala211("0.11.1", "0.12.1").value,
  "io.circe"      %% "circe-literal"  % onScala211("0.11.1", "0.12.1").value % Test,
  "org.typelevel" %% "jawn-parser"    % "0.14.2" % Test
)

developers := List(
  Developer("moradology", "Nathan Zimmerman", "npzimmerman@gmail.com", url("https://github.com/moradology")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.circe.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
