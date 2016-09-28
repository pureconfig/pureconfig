package pureconfig.example.conf

import java.nio.file.Path

case class Config(dirwatch: DirWatchConfig)
case class DirWatchConfig(path: Path, filter: String, email: EmailConfig)
case class EmailConfig(host: String, port: Int, message: String, recipients: Set[String], sender: String)
