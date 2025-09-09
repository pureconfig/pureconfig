package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror
import scala.quoted._

private[scala3] object AnyValDerivationMacros {

  /** Derive a `ConfigReader` for a value class. Can only be called after checking that `A` is a value class.
    */
  inline def unsafeDeriveAnyValReader[A](using inline df: DerivationFlow): ConfigReader[A] =
    ${ deriveAnyValReaderImpl[A]('df) }

  private def deriveAnyValReaderImpl[A: Type](df: Expr[DerivationFlow])(using Quotes): Expr[ConfigReader[A]] = {
    import quotes.reflect._

    val wrapperTypeRepr = TypeRepr.of[A]
    val wrapperSymbol = wrapperTypeRepr.typeSymbol
    val underlyingField = wrapperSymbol.declaredFields.head
    val underlyingTypeRepr = wrapperTypeRepr.memberType(underlyingField)

    // derive underlying reader and wrap it into a value class
    underlyingTypeRepr.asType match {
      case '[t] =>
        def wrap(expr: Expr[t]): Expr[A] =
          New(Inferred(wrapperTypeRepr))
            .select(wrapperSymbol.primaryConstructor)
            .appliedTo(expr.asTerm)
            .asExprOf[A]

        '{
          HintsAwareConfigReaderDerivation.summonConfigReader[t](using $df).map(a => ${ wrap('a) })
        }
    }
  }

  /** Derive a `ConfigWriter` for a value class. Can only be called after checking that `A` is a value class.
    */
  inline def unsafeDeriveAnyValWriter[A](using inline df: DerivationFlow): ConfigWriter[A] =
    ${ deriveAnyValWriterImpl[A]('df) }

  private def deriveAnyValWriterImpl[A: Type](df: Expr[DerivationFlow])(using Quotes): Expr[ConfigWriter[A]] = {
    import quotes.reflect._

    val wrapperTypeRepr = TypeRepr.of[A]
    val wrapperSymbol = wrapperTypeRepr.typeSymbol
    val underlyingField = wrapperSymbol.declaredFields.head
    val underlyingTypeRepr = wrapperTypeRepr.memberType(underlyingField)

    // derive underlying writer and unwrap it from a value class
    underlyingTypeRepr.asType match {
      case '[t] =>
        def unwrap(expr: Expr[A]): Expr[t] =
          expr.asTerm
            .select(underlyingField)
            .appliedToArgss(Nil)
            .asExprOf[t]

        '{
          HintsAwareConfigWriterDerivation.summonConfigWriter[t](using $df).contramap[A](a => ${ unwrap('a) })
        }
    }
  }

  inline def isAnyVal[A]: Boolean = ${ isAnyValImpl[A] }

  def isAnyValImpl[A: Type](using Quotes): Expr[Boolean] = {
    import quotes.reflect._

    Expr(TypeRepr.of[A] <:< TypeRepr.of[AnyVal])
  }
}
