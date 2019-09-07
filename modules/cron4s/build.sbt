name := "pureconfig-cron4s"

libraryDependencies += {
  val cron4sVersion = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n < 12 => "0.5.0"
    case _                      => "0.6.0-M2"
  }
  
  "com.github.alonsodomin.cron4s" %% "cron4s-core" % cron4sVersion
}

developers := List(
  Developer("bardurdam", "Bárður Viberg Dam", "bardurdam@gmail.com", url("https://github.com/bardurdam")))

osgiSettings

OsgiKeys.exportPackage := Seq("pureconfig.module.cron4s.*")
OsgiKeys.privatePackage := Seq()
OsgiKeys.importPackage := Seq(s"""scala.*;version="[${scalaBinaryVersion.value}.0,${scalaBinaryVersion.value}.50)"""", "*")
