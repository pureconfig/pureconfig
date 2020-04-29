name := "pureconfig-scalaz"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.30",
  "org.scalaz" %% "scalaz-scalacheck-binding" % "7.3.0" % "test"
)

developers := List(
  Developer("ChernikovP", "Pavel Chernikov", "chernikov.pavel92@gmail.com", url("https://github.com/ChernikovP"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.scalaz.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
