name := "pureconfig-joda"

organization := "com.github.pureconfig"

homepage := Some(url("https://github.com/pureconfig/pureconfig"))

licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/"))

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  ("joda-time" % "joda-time" % Dependencies.Version.joda),
  ("org.joda" % "joda-convert" % Dependencies.Version.jodaConvert),
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest,
  Dependencies.scalaCheck,
  Dependencies.scalaCheckShapeless
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
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
    </developers>)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.joda.*")

OsgiKeys.privatePackage := Seq()

OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
