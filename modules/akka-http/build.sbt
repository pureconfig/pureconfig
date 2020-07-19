name := "pureconfig-akka-http"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.11")) }

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.31" % Provided,
  "com.typesafe.akka" %% "akka-http" % "10.1.12")

developers := List(
  Developer("himanshu4141", "Himanshu Yadav", "himanshu4141@gmail.com", url("https://github.com/himanshu4141")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.akkahttp.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
