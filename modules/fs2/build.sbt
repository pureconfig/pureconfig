name := "pureconfig-fs2"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.0.1",
  "co.fs2" %% "fs2-io" % "2.0.1")

developers := List(
  Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.fs2.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
