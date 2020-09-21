name := "pureconfig-sttp"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.model" %% "core" % "1.1.4"
)

developers := List(Developer("bszwej", "Bartlomiej Szwej", "bszwej@gmail.com", url("https://github.com/bszwej")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.sttp.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
