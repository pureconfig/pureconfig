name := "pureconfig-cats-effect"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.13")) }

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "2.0.0")

developers := List(
  Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.catseffect.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
