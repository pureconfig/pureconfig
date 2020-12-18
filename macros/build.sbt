import Dependencies.Version._

name := "pureconfig-macros"

crossScalaVersions := Seq(scala211, scala212, scala213, scala30)

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
      )
    case _ => Seq.empty
  }
}

// Scala has excessive warnings about unused implicits on macro exps (https://github.com/scala/bug/issues/10270)
scalacOptions ~= { _.filterNot(_.contains("-Ywarn-unused")) }

developers := List(
  Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog"))
)
