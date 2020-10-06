package pureconfig

/** Trait allowing `ConfigWriter` instances exported via `Exported` to be used.
  *
  * This trait should be the last to be mixed into `ConfigWriter`'s companion object so that exported writers will
  * always have lower precedence than `ConfigWriter` instances exposed as implicits through the usual means.
  */
trait ExportedWriters {
  implicit def exportedWriter[A](implicit exported: Exported[ConfigWriter[A]]): ConfigWriter[A] = exported.instance
}

object ExportedWriters extends ExportedWriters
