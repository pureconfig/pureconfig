import Dependencies.Version._

name := "pureconfig-zio-config"

crossScalaVersions := Seq(scala212, scala213)

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-config-typesafe" % "3.0.7"
)
