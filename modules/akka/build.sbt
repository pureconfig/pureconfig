name := "pureconfig-akka"

// akka 2.4 isn't published for Scala 2.10
crossScalaVersions ~= { oldVersions => oldVersions.filterNot(_.startsWith("2.10")) }

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.19",
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
    <developer>
      <id>ruippeixotog</id>
      <name>Rui Gonçalves</name>
      <url>https://github.com/ruippeixotog</url>
    </developer>
  </developers>
}

OsgiKeys.exportPackage := Seq("pureconfig.module.akka.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
