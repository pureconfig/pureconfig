name := "pureconfig-joda"

libraryDependencies ++= Seq(
  ("joda-time" % "joda-time" % Dependencies.Version.joda),
  ("org.joda" % "joda-convert" % Dependencies.Version.jodaConvert),
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest,
  Dependencies.scalaCheck,
  Dependencies.scalaCheckShapeless)

pomExtra := {
  <scm>
    <url>git@github.com:pureconfig/pureconfig.git</url>
    <connection>scm:git:git@github.com:pureconfig/pureconfig.git</connection>
  </scm>
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

OsgiKeys.exportPackage := Seq("pureconfig.module.joda.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
