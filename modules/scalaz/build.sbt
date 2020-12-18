import Dependencies.Version._

name := "pureconfig-scalaz"

crossScalaVersions := Seq(scala211, scala212, scala213)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.3.2",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.3.2" % "test"
)

mdocScalacOptions += "-Ypartial-unification"

developers := List(
  Developer("ChernikovP", "Pavel Chernikov", "chernikov.pavel92@gmail.com", url("https://github.com/ChernikovP"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.scalaz.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
