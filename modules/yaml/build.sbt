import Dependencies.Version._

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "2.1")

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
