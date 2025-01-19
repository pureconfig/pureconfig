addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.0")
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.4.4")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-boilerplate" % "0.7.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.6.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.14")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.2.2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")

libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.16"

// taken from https://github.com/scala/bug/issues/12632
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
