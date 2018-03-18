name := "pureconfig-akka"

// akka 2.4 isn't published for Scala 2.10
crossScalaVersions ~= { oldVersions => oldVersions.filterNot(_.startsWith("2.10")) }

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.19",
  Dependencies.scalaMacrosParadise,
  Dependencies.scalaTest)

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.akka.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
