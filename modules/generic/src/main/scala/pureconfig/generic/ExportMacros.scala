package pureconfig.generic

import scala.reflect.macros.blackbox

import pureconfig._

/**
 * Macros used to circunvent divergence checker restrictions in the compiler.
 */
@macrocompat.bundle
class ExportMacros(val c: blackbox.Context) {
  import c.universe._

  final def exportDerivedReader[A](implicit a: c.WeakTypeTag[A]): c.Expr[Exported[ConfigReader[A]]] = {
    c.typecheck(q"_root_.shapeless.lazily[_root_.pureconfig.generic.DerivedConfigReader[$a]]", silent = true) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"Unable to infer value of type $a")
      case t =>
        c.Expr[Exported[ConfigReader[A]]](
          q"new _root_.pureconfig.Exported($t: _root_.pureconfig.ConfigReader[$a])")
    }
  }

  final def exportDerivedWriter[A](implicit a: c.WeakTypeTag[A]): c.Expr[Exported[ConfigWriter[A]]] = {
    c.typecheck(q"_root_.shapeless.lazily[_root_.pureconfig.generic.DerivedConfigWriter[$a]]", silent = true) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"Unable to infer value of type $a")
      case t =>
        c.Expr[Exported[ConfigWriter[A]]](
          q"new _root_.pureconfig.Exported($t: _root_.pureconfig.ConfigWriter[$a])")
    }
  }
}
