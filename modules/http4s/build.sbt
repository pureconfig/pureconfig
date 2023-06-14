import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("org.http4s" %% "http4s-core" % "0.23.20")

developers := List(
  Developer("jcranky", "Paulo Siqueira", "paulo.siqueira@gmail.com", url("https://github.com/jcranky"))
)
