name := "pureconfig-squants"

libraryDependencies ++= Seq(
  "org.typelevel" %% "squants" % "1.3.0",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest,
  Dependencies.scalaCheck)

pomExtra := {
  <developers>
    <developer>
      <id>melrief</id>
      <name>Mario Pastorelli</name>
      <url>https://github.com/melrief</url>
    </developer>
    <developer>
      <id>cranst0n</id>
      <name>Ian McIntosh</name>
      <url>https://github.com/cranst0n</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.squants.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
