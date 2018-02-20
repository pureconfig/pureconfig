name := "pureconfig-http4s"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-core" % "0.18.0",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest
)

crossScalaVersions := Seq("2.11.11", "2.12.4")

pomExtra := {
  <developers>
    <developer>
      <id>jcranky</id>
      <name>Paulo Siqueira</name>
      <url>https://github.com/jcranky</url>
    </developer>
  </developers>
}

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.modules.http4s.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
