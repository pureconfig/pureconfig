package pureconfig

/**
  * Trait allowing `ConfigReader` instances exported via `Exported` to be used.
  *
  * This trait should be the last to be mixed into `ConfigReader`'s companion object so that exported readers will
  * always have lower precedence than `ConfigReader` instances exposed as implicits through the usual means.
  */
trait ExportedReaders {
  implicit def exportedReader[A](implicit exported: Exported[ConfigReader[A]]): ConfigReader[A] = exported.instance
}

object ExportedReaders extends ExportedReaders
