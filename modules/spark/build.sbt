import Dependencies.Version._

name := "pureconfig-spark"

crossScalaVersions := Seq(scala212) //Spark does not support Scala 2.13 yet

lazy val isScala212 = settingKey[Boolean]("Is the scala version 2.12.")
isScala212 := scalaVersion.value == scala212

//scalafix does not honour `crossScalaVersions` so it needs to be disabled explicitly.
scalafix := isScala212.value
scalafixAll := isScala212.value
scalafixOnCompile := isScala212.value

libraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.1.2" % "provided")
mdocLibraryDependencies ++= Seq("org.apache.spark" %% "spark-sql" % "3.1.2")

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.spark.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
