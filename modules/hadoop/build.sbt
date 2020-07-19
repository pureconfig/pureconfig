name := "pureconfig-hadoop"

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "3.3.0" % "provided,Tut")

developers := List(
  Developer("lmnet", "Yuriy Badalyantc", "lmnet89@gmail.com", url("https://github.com/lmnet")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.hadoop.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
