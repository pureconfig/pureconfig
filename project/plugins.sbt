addSbtPlugin("com.47deg"         % "sbt-microsites"  % "0.7.27")
addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.11")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % "1.1.2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"        % "0.9.5")
addSbtPlugin("io.spray"          % "sbt-boilerplate" % "0.6.1")
addSbtPlugin("org.scalariform"   % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scoverage"     % "sbt-coveralls"   % "1.2.7")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"   % "1.6.0-RC1")
addSbtPlugin("org.tpolecat"      % "tut-plugin"      % "0.6.12")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % "2.4")

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.26"
