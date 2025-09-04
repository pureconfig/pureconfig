import Dependencies.Version._

crossScalaVersions := Seq(scala213, scala3)

libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "2.5")

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)

Test / fork := true
Test / envVars := Map("EXISTING_VAR" -> "50")
