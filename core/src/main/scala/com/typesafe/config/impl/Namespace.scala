package com.typesafe.config.impl

import scala.annotation.tailrec

/**
 * A public wrapper over the package-private `Path` at `com.typesafe.config.impl`. This exposes a way to convert
 * Typesafe Config namespaces from strings to a list of keys and vice-versa using their internal path parser.
 */
object Namespace {
  @tailrec private[this] def toList(path: Path, acc: List[String] = Nil): List[String] = path match {
    case null => acc.reverse
    case _ => toList(path.remainder, path.first :: acc)
  }

  /**
   * Parses a namespace into a list of keys from the root to the leaf value.
   *
   * @param namespace the namespace to parse
   * @return a list of keys, from the root to the leaf value, representing the given namespace.
   */
  def parse(namespace: String): List[String] =
    if (namespace.isEmpty) Nil else toList(Path.newPath(namespace))

  /**
   * Converts a list of keys to a path string compatible with the `get` methods in `Config`.
   *
   * @param path the list of keys to convert to a string
   * @return a path string compatible with the `get` methods in `Config`.
   */
  def toString(path: List[String]): String =
    if (path.isEmpty) "" else new Path(path: _*).render
}
