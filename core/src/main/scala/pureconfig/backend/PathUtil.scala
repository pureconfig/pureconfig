package pureconfig.backend

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigUtil

/** An utility class to convert Typesafe Config path expressions to lists of keys and vice-versa, accepting empty path
  * expressions and lists in addition to the values supported by `ConfigUtil`.
  */
object PathUtil {

  /** Parses a path expression into a list of keys from the root to the leaf value.
    *
    * @param path
    *   the path to parse
    * @return
    *   a list of keys, from the root to the leaf value, representing the given namespace.
    */
  def splitPath(path: String): List[String] =
    if (path.isEmpty) Nil else ConfigUtil.splitPath(path).asScala.toList

  /** Converts a list of keys to a path expression compatible with the `get` methods in `Config`.
    *
    * @param elements
    *   the list of keys to convert to a string
    * @return
    *   a path string compatible with the `get` methods in `Config`.
    */
  def joinPath(elements: List[String]): String =
    if (elements.isEmpty) "" else ConfigUtil.joinPath(elements: _*)
}
