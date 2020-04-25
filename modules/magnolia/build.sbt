import Dependencies._

name := "pureconfig-magnolia"

crossScalaVersions ~= { _.filterNot(_.startsWith("2.11")) }

libraryDependencies ++= Seq(
  // exclude scala-compiler from runtime classpath as magnolia accidentally pulls it
  // please remove exclusion once magnolia is updated to >= 0.15.1
  // see https://github.com/pureconfig/pureconfig/pull/744#issuecomment-619291536
  "com.propensive" %% "magnolia" % "0.15.0" exclude ("org.scala-lang", "scala-compiler"),
  scalaCheckShapeless % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.magnolia.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
