import Dependencies._

name := "pureconfig-generic"

libraryDependencies ++= Seq(
  shapeless.value,
  scalaMacrosParadise,
  typesafeConfig,
  scalaTest,
  scalaCheck,
  scalaCheckShapeless.value)

developers := List(
  Developer("melrief", "Mario Pastorelli", "pastorelli.mario@gmail.com", url("https://github.com/melrief")),
  Developer("leifwickland", "Leif Wickland", "leifwickland@gmail.com", url("https://github.com/leifwickland")),
  Developer("jcazevedo", "Joao Azevedo", "joao.c.azevedo@gmail.com", url("https://github.com/jcazevedo")),
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")),
  Developer("derekmorr", "Derek Morr", "morr.derek@gmail.com", url("https://github.com/derekmorr")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.generic.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
