name := "pureconfig-squants"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.13")) }

libraryDependencies ++= Seq(
  "org.typelevel" %% "squants" % "1.3.0")  // blocked by https://github.com/typelevel/squants/issues/321

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("cranst0n", "Ian McIntosh", "cranston.ian@gmail.com", url("https://github.com/cranst0n")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.squants.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
