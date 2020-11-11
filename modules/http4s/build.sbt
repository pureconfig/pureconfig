name := "pureconfig-http4s"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.11")) }

libraryDependencies ++= Seq("org.http4s" %% "http4s-core" % "0.21.9")

developers := List(
  Developer("jcranky", "Paulo Siqueira", "paulo.siqueira@gmail.com", url("https://github.com/jcranky"))
)

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.http4s.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
