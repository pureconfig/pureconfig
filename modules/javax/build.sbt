name := "pureconfig-javax"

libraryDependencies ++= Seq(
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
  <developers>
    <developer>
      <id>derekmorr</id>
      <name>Derek Morr</name>
      <url>https://github.com/derekmorr</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.javax.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
