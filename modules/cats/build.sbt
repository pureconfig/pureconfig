name := "pureconfig-cats"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "0.9.0",
  "org.typelevel" %% "cats-laws" % "0.9.0" % "test",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest,
  Dependencies.scalaCheck)

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
    <developer>
      <id>ruippeixotog</id>
      <name>Rui Gonçalves</name>
      <url>https://github.com/ruippeixotog</url>
    </developer>
  </developers>
}

OsgiKeys.exportPackage := Seq("pureconfig.module.cats.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
