import Dependencies.Version._

name := "pureconfig-zio-config"

crossScalaVersions := Seq(scala212, scala213)

val zioConfigVersion = "1.0.6"

val zioConfigMagnoliaDependency = "dev.zio" %% "zio-config-magnolia" % zioConfigVersion

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  zioConfigMagnoliaDependency % Test
)

mdocLibraryDependencies += zioConfigMagnoliaDependency

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.zioconfig.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(
  s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""",
  "*"
)
