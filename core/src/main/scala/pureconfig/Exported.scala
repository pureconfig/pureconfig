package pureconfig

/** A wrapper for type class instances intended to allow implicits in scope by `import` statements to have lower
  * priority than implicits in the relevant companion objects. This behavior requires type classes to provide a bridge
  * in their companion objects, as done by `ConfigReader` (see [[pureconfig.ExportedReaders]] ) and `ConfigWriter` (see
  * [[pureconfig.ExportedWriters]] ).
  *
  * @param instance
  *   the type class instance to wrap
  * @tparam A
  *   the type class
  */
case class Exported[A](instance: A) extends AnyVal
