import Dependencies._

name := "pureconfig-macros"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.typelevel" %% "macro-compat" % "1.1.1",
  shapeless % "test",
  scalaMacrosParadise,
  scalaCheck)

pomExtra := {
  <developers>
    <developer>
      <id>ruippeixotog</id>
      <name>Rui Gonçalves</name>
      <url>https://github.com/ruippeixotog</url>
    </developer>
  </developers>
}
