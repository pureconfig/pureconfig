package pureconfig

import java.util.regex.Pattern

import scala.util.matching.Regex

import org.scalactic.Equality
import org.scalactic.TypeCheckedTripleEquals._

package object equality {

  implicit final val PatternEquality = new Equality[Pattern] {
    def areEqual(a: Pattern, b: Any): Boolean =
      b match {
        case bp: Pattern => a.pattern === bp.pattern
        case _ => false
      }
  }

  implicit final val RegexEquality = new Equality[Regex] {
    override def areEqual(a: Regex, b: Any): Boolean =
      b match {
        case r: Regex => PatternEquality.areEqual(a.pattern, r.pattern)
        case _ => false
      }
  }

}
