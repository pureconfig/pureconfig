name := "pureconfig-cats"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.13")) }

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"  % "2.0.0",
  "org.typelevel" %% "cats-laws"  % "2.0.0"  % "test",
  "org.typelevel" %% "discipline" % "0.11.1" % "test")

developers := List(
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.cats.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
