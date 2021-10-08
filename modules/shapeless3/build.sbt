import Dependencies.Version._

name := "pureconfig-shapeless3"

crossScalaVersions := Seq(scala30)

libraryDependencies ++= Seq(
  "org.typelevel" %% "shapeless3-deriving" % "3.0.1"
)

MainScalaVersionHack.projectSettings
