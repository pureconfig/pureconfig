name := "pureconfig-cats-effect"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.10.1",
  Dependencies.scalaMacrosParadise)

developers := List(
  Developer("keirlawson", "Keir Lawson", "keirlawson@gmail.com", url("https://github.com/keirlawson")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.catseffect.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
