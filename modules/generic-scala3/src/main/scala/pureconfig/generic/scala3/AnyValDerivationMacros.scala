package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.quoted._

private[pureconfig] object AnyValDerivationMacros {
  inline def deriveAnyValReader[A <: AnyVal]: ConfigReader[A] = ${ deriveAnyValReaderImpl[A] }

  def deriveAnyValReaderImpl[A <: AnyVal: Type](using Quotes): Expr[ConfigReader[A]] = {
    import quotes.reflect._

    val typeRepr = TypeRepr.of[A]
    val wrapperSymbol = typeRepr.typeSymbol
    val underlyingField = wrapperSymbol.declaredFields.head
    val underlyingTpe = typeRepr.memberType(underlyingField)

    // derive underlying reader and wrap it in a value class constructor
    underlyingTpe.asType match {
      case '[t] =>
        val underlyingReader = Expr
          .summon[ConfigReader[t]]
          .getOrElse {
            report.errorAndAbort(
              s"Cannot summon ConfigReader for value class ${typeRepr.show} wrapping ${underlyingTpe.show}."
            )
          }

        def wrapperConstructor(expr: Expr[t]) =
          New(Inferred(typeRepr))
            .select(wrapperSymbol.primaryConstructor)
            .appliedTo(expr.asTerm)
            .asExprOf[A]

        '{
          $underlyingReader.map(a => ${ wrapperConstructor('a) })
        }
    }
  }
}
