package pureconfig

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * Helper methods for testing the behavior of `Derivation`.
  */
object DerivationChecks {

  /**
    * A version of the `shapeless.test.illTyped` macro that allows expected error messages with multiple lines, as well
    * as whitespace before and after the message.
    *
    * @param code the code to check for non-compilation
    * @param expected the expected error message as a variable number of lines
    */
  def illTyped(code: String, expected: String*): Unit = macro DerivationChecksMacros.illTyped
}

class DerivationChecksMacros(val c: blackbox.Context) {
  import c.universe._

  def illTyped(code: Expr[String], expected: Expr[String]*): Expr[Unit] = {
    val linesStr = expected.map(_.tree).map { case Literal(Constant(line: String)) => line }
    val expectedStr = "\\s*" + linesStr.mkString("\n") + "\\s*"
    c.Expr(q"_root_.shapeless.test.illTyped($code, $expectedStr)")
  }
}
