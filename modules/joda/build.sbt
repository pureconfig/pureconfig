import Dependencies.Version._

name := "pureconfig-joda"

crossScalaVersions := Seq(scala211, scala212, scala213)

libraryDependencies ++= Seq("joda-time" % "joda-time" % "2.10.8", "org.joda" % "joda-convert" % "2.2.1")

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.joda.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
