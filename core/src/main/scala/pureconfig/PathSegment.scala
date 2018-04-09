package pureconfig

import language.implicitConversions

sealed trait PathSegment extends Product with Serializable

object PathSegment {
  final case class Key(k: String) extends PathSegment
  final case class Index(i: Int) extends PathSegment

  implicit def intToPathSegment(i: Int): PathSegment = Index(i)
  implicit def stringToPathSegment(k: String): PathSegment = Key(k)
}
