crossScalaVersions ~= { oldVersions => oldVersions.filterNot(_.startsWith("2.11")) }
