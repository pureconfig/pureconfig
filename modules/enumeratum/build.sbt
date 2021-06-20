import Dependencies.Version._

name := "pureconfig-enumeratum"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq("com.beachape" %% "enumeratum" % "1.7.0")

developers := List(Developer("aeons", "Bjørn Madsen", "bm@aeons.dk", url("https://github.com/aeons")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.enumeratum.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
