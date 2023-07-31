resolvers ++= Resolver.sonatypeOssRepos("snapshots")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.0")
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.4.3")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("io.spray" % "sbt-boilerplate" % "0.6.1")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.3.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("net.ruippeixotog" % "sbt-coveralls" % "1.3.9+2-56cb75f0+20230731-2315-SNAPSHOT")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")

libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.7"

// taken from https://github.com/scala/bug/issues/12632
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
