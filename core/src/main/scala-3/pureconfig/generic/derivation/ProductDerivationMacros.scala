package pureconfig
package generic
package derivation

import scala.quoted._

private[pureconfig] object ProductDerivationMacros {
  type DefaultValue = Option[() => Any]

  /** Materializes the default values of the constructor parameters of `T` as a vector of thunks, with `None` for
    * parameters without a default.
    *
    * @param size
    *   the number of constructor parameters of `T`
    */
  inline def getDefaults[T](inline size: Int): Vector[DefaultValue] = ${ getDefaultsImpl[T]('size) }

  private def getDefaultsImpl[T](size: Expr[Int])(using Quotes, Type[T]): Expr[Vector[DefaultValue]] = {
    import quotes.reflect._

    val n = size.valueOrAbort
    val typeRepr = TypeRepr.of[T]

    def defaultMethodAt(i: Int) =
      typeRepr.typeSymbol.companionClass.declaredMethod(s"$$lessinit$$greater$$default$$$i").headOption
    def callMethod(symbol: Symbol) =
      Ref(typeRepr.typeSymbol.companionModule).select(symbol).appliedToTypes(typeRepr.typeArgs)

    val expr = Expr.ofSeq {
      (1 to n).map { i =>
        defaultMethodAt(i) match {
          case Some(value) => '{ Some(() => ${ callMethod(value).asExpr }) }
          case None => Expr(None)
        }
      }
    }

    '{ $expr.toVector }
  }
}
