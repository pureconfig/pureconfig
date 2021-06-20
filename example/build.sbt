name := "pureconfig-example"
version := "1.0"
scalaVersion := "2.12.14"

val VersionPattern = """ThisBuild / version := "([^"]*)"""".r
val pureconfigVersion = IO.read(file("../version.sbt")).trim match {
  case VersionPattern(ver) => ver
  case ex =>
    println(s"""'$ex'""")
    throw new Exception("Could not parse PureConfig version")
}

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % pureconfigVersion

crossScalaVersions := Seq("2.12.14", "2.13.6")

val versionSpecificFlags =
  Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) =>
        // Excluding -byname-implicit is required for Scala 2.13 due to https://github.com/scala/bug/issues/12072
        Seq("-Xlint:_,-byname-implicit", "-Vimplicits")
      case _ =>
        Seq("-Xlint:_")
    }
  }

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:experimental.macros",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
) ++ versionSpecificFlags.value

scalafmtOnCompile := true
