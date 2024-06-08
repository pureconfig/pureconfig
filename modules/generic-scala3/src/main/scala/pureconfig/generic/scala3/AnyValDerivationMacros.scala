package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.quoted._

private[pureconfig] object AnyValDerivationMacros {
  inline def deriveAnyValReader[A <: AnyVal]: ConfigReader[A] = ${ deriveAnyValReaderImpl[A] }

  def deriveAnyValReaderImpl[A <: AnyVal: Type](using Quotes): Expr[ConfigReader[A]] = {
    import quotes.reflect._

    val wrapperTypeRepr = TypeRepr.of[A]
    val wrapperSymbol = wrapperTypeRepr.typeSymbol
    val underlyingField = wrapperSymbol.declaredFields.head
    val underlyingTypeRepr = wrapperTypeRepr.memberType(underlyingField)

    // derive underlying reader and wrap it in a value class constructor
    underlyingTypeRepr.asType match {
      case '[t] =>
        val underlyingReader = Expr
          .summon[ConfigReader[t]]
          .getOrElse {
            // TODO: derive reader for underlying type if it's not found ?
            report.errorAndAbort(
              s"Cannot summon ConfigReader for value class ${wrapperTypeRepr.show} wrapping ${underlyingTypeRepr.show}."
            )
          }

        def wrap(expr: Expr[t]): Expr[A] =
          New(Inferred(wrapperTypeRepr))
            .select(wrapperSymbol.primaryConstructor)
            .appliedTo(expr.asTerm)
            .asExprOf[A]

        '{
          $underlyingReader.map(a => ${ wrap('a) })
        }
    }
  }

  inline def deriveAnyValWriter[A <: AnyVal]: ConfigWriter[A] = ${ deriveAnyValWriterImpl[A] }

  def deriveAnyValWriterImpl[A <: AnyVal: Type](using Quotes): Expr[ConfigWriter[A]] = {
    import quotes.reflect._

    val wrapperTypeRepr = TypeRepr.of[A]
    val wrapperSymbol = wrapperTypeRepr.typeSymbol
    val underlyingField = wrapperSymbol.declaredFields.head
    val underlyingTypeRepr = wrapperTypeRepr.memberType(underlyingField)

    // derive underlying writer and unwrap it from a value class
    underlyingTypeRepr.asType match {
      case '[t] =>
        val underlyingWriter = Expr
          .summon[ConfigWriter[t]]
          .getOrElse {
            // TODO: derive reader for underlying type if it's not found ?
            report.errorAndAbort(
              s"Cannot summon ConfigWriter for value class ${wrapperTypeRepr.show} wrapping ${underlyingTypeRepr.show}."
            )
          }

        def unwrap(expr: Expr[A]): Expr[t] =
          expr.asTerm
            .select(underlyingField)
            .appliedToArgss(Nil)
            .asExprOf[t]

        '{
          $underlyingWriter.contramap[A](a => ${ unwrap('a) })
        }
    }
  }

}
