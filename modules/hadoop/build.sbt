name := "pureconfig-hadoop"

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "3.0.0" % "provided,tut",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
  <developers>
    <developer>
      <id>lmnet</id>
      <name>Yuriy Badalyantc</name>
      <url>https://github.com/lmnet</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.hadoop.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
