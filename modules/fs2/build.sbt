name := "pureconfig-fs2"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "0.10.2",
  "co.fs2" %% "fs2-io" % "0.10.2",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

// fs2 0.10 isn't published for Scala 2.10
crossScalaVersions ~= { oldVersions => oldVersions.filterNot(_.startsWith("2.10")) }

pomExtra := {
  <scm>
    <url>git@github.com:pureconfig/pureconfig.git</url>
    <connection>scm:git:git@github.com:pureconfig/pureconfig.git</connection>
  </scm>
    <developers>
      <developer>
        <id>keirlawson</id>
        <name>Keir Lawson</name>
        <url>https://github.com/keirlawson/</url>
      </developer>
    </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.fs2.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")

scalacOptions in Compile ++= Seq("-Ypartial-unification")