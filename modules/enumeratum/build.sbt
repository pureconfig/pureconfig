import Dependencies.Version._
import Utilities.forScalaVersions

crossScalaVersions := Seq(scala212, scala213, scala3)

libraryDependencies ++= Seq("com.beachape" %% "enumeratum" % "1.7.5")

developers := List(Developer("aeons", "Bjørn Madsen", "bm@aeons.dk", url("https://github.com/aeons")))

lazy val extraScalacOptions = forScalaVersions {
  case (3, _) =>
    List(
      "-Yretain-trees" // needed for using ValueEnums on scala3 https://github.com/lloydmeta/enumeratum/blob/master/README.md?plain=1#L28
    )
  case _ => List.empty
}

scalacOptions ++= extraScalacOptions.value
