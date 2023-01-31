import Dependencies.Version._

name := "pureconfig-http4s"

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("org.http4s" %% "http4s-core" % "0.22.14")

developers := List(
  Developer("jcranky", "Paulo Siqueira", "paulo.siqueira@gmail.com", url("https://github.com/jcranky"))
)
