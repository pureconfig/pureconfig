addSbtPlugin("com.47deg"         % "sbt-microsites"  % "0.7.22")
addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.9")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % "1.1.2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"        % "0.9.4")
addSbtPlugin("io.spray"          % "sbt-boilerplate" % "0.6.1")
addSbtPlugin("org.scalariform"   % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scoverage"     % "sbt-coveralls"   % "1.2.5")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"   % "1.5.1")
addSbtPlugin("org.tpolecat"      % "tut-plugin"      % "0.6.7")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % "2.3")

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25"
