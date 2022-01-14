import Dependencies.Version._

name := "pureconfig-zio-config"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-config-typesafe" % "2.0.0"
)
