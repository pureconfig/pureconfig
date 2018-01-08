name := "pureconfig-enumeratum"

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % "1.5.12",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
  <developers>
    <developer>
      <id>aeons</id>
      <name>Bjørn Madsen</name>
      <url>https://github.com/aeons</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.enumeratum.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
