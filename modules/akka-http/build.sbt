import Dependencies.Version._

name := "pureconfig-akka-http"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.14" % "provided",
  "com.typesafe.akka" %% "akka-http" % "10.2.4"
)
mdocLibraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.14"
)

developers := List(
  Developer("himanshu4141", "Himanshu Yadav", "himanshu4141@gmail.com", url("https://github.com/himanshu4141"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.akkahttp.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
