name := "pureconfig-javax"

libraryDependencies ++= Seq(
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
  <scm>
    <url>git@github.com:pureconfig/pureconfig.git</url>
    <connection>scm:git:git@github.com:pureconfig/pureconfig.git</connection>
  </scm>
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
