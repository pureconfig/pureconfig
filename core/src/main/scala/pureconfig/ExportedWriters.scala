package pureconfig

trait ExportedWriters {
  implicit def exportedWriter[A](implicit exported: Exported[ConfigWriter[A]]): ConfigWriter[A] = exported.instance
}

object ExportedWriters extends ExportedWriters
