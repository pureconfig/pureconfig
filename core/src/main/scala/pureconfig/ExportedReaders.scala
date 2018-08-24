package pureconfig

trait ExportedReaders {
  implicit def exportedReader[A](implicit exported: Exported[ConfigReader[A]]): ConfigReader[A] = exported.instance
}

object ExportedReaders extends ExportedReaders
