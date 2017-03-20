import Dependencies._

name := "pureconfig"

organization := "com.github.melrief"

homepage := Some(url("https://github.com/melrief/pureconfig"))

licenses := Seq("Mozilla Public License, version 2.0" -> url("https://www.mozilla.org/MPL/2.0/"))

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  shapeless,
  scalaMacrosParadise,
  typesafeConfig,
  scalaTest,
  joda % "test",
  jodaConvert % "test",
  scalaCheck,
  scalaCheckShapeless
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
      <url>git@github.com:melrief/pureconfig.git</url>
      <connection>scm:git:git@github.com:melrief/pureconfig.git</connection>
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
      <developer>
        <id>jcazevedo</id>
        <name>Joao Azevedo</name>
        <url>https://github.com/jcazevedo</url>
      </developer>
      <developer>
        <id>ruippeixotog</id>
        <name>Rui Gonçalves</name>
        <url>https://github.com/ruippeixotog</url>
      </developer>
      <developer>
        <id>derekmorr</id>
        <name>Derek Morr</name>
        <url>https://github.com/derekmorr</url>
      </developer>
    </developers>)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.*")

OsgiKeys.privatePackage := Seq()

OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
