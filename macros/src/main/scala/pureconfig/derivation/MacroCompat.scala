package pureconfig.derivation

import scala.reflect.macros.{ TypecheckException, whitebox }

import pureconfig.Derivation

/**
 * An API for macro operations that require access to macro or even compiler internals. Since they rely on
 * non-public APIs, the code may be distinct between Scala major versions and even between minor versions.
 */
trait MacroCompat {
  val c: whitebox.Context

  import c.universe._

  // since we are inside a whitebox implicit macro, error messages from `c.abort` as not printed. A trick must be used
  // to make the compiler print our custom message. That's done by setting a @implicitNotFound annotation on our
  // `Derivation` class (idea taken from shapeless `Lazy`).
  def setImplicitNotFound(msg: String): Unit = {
    import c.internal.decorators._
    val infTree = c.typecheck(q"""new _root_.scala.annotation.implicitNotFound($msg)""", silent = false)
    typeOf[Derivation[_]].typeSymbol.setAnnotations(Annotation(infTree))
  }

  // This should be simply defined as `c.inferImplicitValue(c.weakTypeOf[A])`, but divergent implicits are wrongly
  // reported up to Scala 2.12.2. See https://github.com/scala/bug/issues/10398 for more information.
  def inferImplicitValueCompat(typ: Type): Tree = {
    val cc = c.asInstanceOf[scala.reflect.macros.contexts.Context]
    val enclosingTree =
      cc.openImplicits.headOption.map(_.tree)
        .orElse(cc.enclosingMacros.lastOption.map(_.macroApplication))
        .getOrElse(EmptyTree).asInstanceOf[cc.universe.analyzer.global.Tree]

    val res: cc.Tree = cc.universe.analyzer.inferImplicit(
      enclosingTree, typ.asInstanceOf[cc.Type], false, cc.callsiteTyper.context, true, false, cc.enclosingPosition,
      (pos, msg) => throw TypecheckException(pos, msg))

    res.asInstanceOf[c.Tree]
  }
}
