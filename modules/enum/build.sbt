name := "pureconfig-enum"

libraryDependencies ++= Seq(
  "org.julienrf" %% "enum" % "3.1",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
  <developers>
    <developer>
      <id>melrief</id>
      <name>Mario Pastorelli</name>
      <url>https://github.com/melrief</url>
    </developer>
    <developer>
      <id>leifwickland</id>
      <name>Leif Wickland</name>
      <url>https://github.com/leifwickland</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.enum.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
