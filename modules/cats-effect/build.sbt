name := "pureconfig-cats-effect"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.8",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

pomExtra := {
    <developers>
      <developer>
        <id>keirlawson</id>
        <name>Keir Lawson</name>
        <url>https://github.com/keirlawson/</url>
      </developer>
    </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.catseffect.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
