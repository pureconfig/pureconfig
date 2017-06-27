package pureconfig

import scala.language.experimental.macros
import scala.reflect.macros.{ TypecheckException, Universe, whitebox }

sealed trait Derivation[A] {
  def value: A
}

object Derivation {

  // A derivation for which an implicit value of `A` could be found.
  case class Successful[A](value: A) extends Derivation[A]

  // A derivation for which an implicit `A` could be found. This is only used internally by the `materializeDerivation`
  // macro - when a derivation requested directly by a user is successfully materialized, it is guaranteed to be a
  // `Derivation.Successful`.
  private[pureconfig] case class Failed[A]() extends Derivation[A] {
    def value = throw new IllegalStateException("Illegal Derivation")
  }

  implicit def materializeDerivation[A]: Derivation[A] = macro DerivationMacros.materializeDerivation[A]
}

@macrocompat.bundle
class DerivationMacros(val c: whitebox.Context) {
  import DerivationMacros._
  import c.universe._

  private[this] implicit class RichType(val t: Type) {
    def toTree: Tree =
      if (t.typeArgs.isEmpty) tq"${t.typeSymbol}"
      else tq"${t.typeConstructor.toTree}[..${t.typeArgs.map(_.toTree)}]"
  }

  // The entrypoint for materializing `Derivation` instances.
  def materializeDerivation[A: WeakTypeTag]: Tree = {

    // if `false`, then this is a `Derivation` triggered inside another `Derivation`
    val isRootDerivation = c.openImplicits.count(_.pre =:= typeOf[Derivation.type]) == 1

    if (isRootDerivation) materializeRootDerivation[A]
    else materializeInnerDerivation[A]
  }

  // Materialize a `Derivation` that was requested in the context of another.
  //
  // In this case a `Derivation` instance is always materialized so that the implicit search does not stop here. We
  // materialize a `Derivation.failed` for missing implicits so that the `Derivation` at the root level can easily find
  // and handle them.
  private[this] def materializeInnerDerivation[A: WeakTypeTag]: Tree = {
    currentPath ::= weakTypeOf[A]

    val res = inferImplicitValue[A] match {
      case EmptyTree =>
        failedDerivations ::= currentPath
        q"_root_.pureconfig.Derivation.Failed[${weakTypeOf[A]}]()"

      case value =>
        q"_root_.pureconfig.Derivation.Successful[${weakTypeOf[A]}]($value)"
    }
    currentPath = currentPath.tail
    res
  }

  // Materialize a `Derivation` at the root level, i.e. not caused by the materialization of another `Derivation`.
  //
  // As usual, it searches for an implicit for `A`. If it fails, the macro aborts as expected, causing an implicit not
  // found error with a basic message. If an implicit is found, it still needs to search the tree found in order to
  // check if some inner derivations materialized a `Derivation.failed`. If that's the case, it collects those failures
  // and prints a nice message.
  private[this] def materializeRootDerivation[A: WeakTypeTag]: Tree = {
    inferImplicitValue[A] match {
      case EmptyTree =>
        // failed to find an implicit at the root level of the derivation; set a generic implicitNotFound message
        // without further information
        setImplicitNotFound(Nil)

        // cause the implicit to fail materializing - the message is ignored
        c.abort(c.enclosingPosition, "")

      case value =>
        // collect the failed derivations in the built implicit tree
        val failed = failedDerivations.asInstanceOf[List[List[Type]]]
        failedDerivations = Nil

        if (failed.isEmpty) {
          q"_root_.pureconfig.Derivation.Successful[${weakTypeOf[A]}]($value)"
        } else {
          // if there are failures, that means one of the inner implicits was not found - set a message with details
          // about the paths failed
          setImplicitNotFound(failed)

          // cause the implicit to fail materializing - the message is ignored
          c.abort(c.enclosingPosition, "")
        }
    }
  }

  // This should be simply defined as `c.inferImplicitValue(c.weakTypeOf[A])`, but divergent implicits are wrongly
  // being reported. See https://github.com/scala/scala-dev/issues/398 for more information.
  private[this] def inferImplicitValue[A: WeakTypeTag]: Tree = {
    val cc = c.asInstanceOf[scala.reflect.macros.contexts.Context]
    val enclosingTree = cc.openImplicits.head.tree.asInstanceOf[cc.universe.analyzer.global.Tree]

    val res: cc.Tree = cc.universe.analyzer.inferImplicit(
      enclosingTree, cc.weakTypeOf[A], false, cc.callsiteTyper.context, true, false, cc.enclosingPosition,
      (pos, msg) => throw TypecheckException(pos, msg))

    res.asInstanceOf[Tree]
  }

  // Prepares and sets the message to be printed for the given failed derivations.
  private[this] def setImplicitNotFound[A: WeakTypeTag](failedDerivations: List[List[Type]]): Unit = {
    val builder = new StringBuilder()

    failedDerivations match {
      case Nil => builder ++= s"could not find ${prettyPrintType(weakTypeOf[A])}\n"
      case _ => builder ++= s"could not derive ${prettyPrintType(weakTypeOf[A])}, because:\n"
    }

    def buildMessage(scopedFailedDerivations: List[List[Type]], depth: Int): Unit = {

      // the distinct first derivation steps that failed
      val distinctDirectReasons: List[Type] =
        scopedFailedDerivations.collect { case typ :: _ => typ }.distinct

      // a list of pairs associating each first derivation step with the list of paths scoped to that step
      val failuresByDirectReason: List[(Type, List[List[Type]])] =
        distinctDirectReasons.map { typ =>
          typ -> scopedFailedDerivations.collect {
            // collect only paths that start with `typ` and do not become empty after scoping it
            case `typ` :: resth :: restl => resth :: restl
          }
        }

      failuresByDirectReason.foreach {
        case (typ, innerReasons) =>
          val msgEnding = if (innerReasons.isEmpty) "" else ", because:"
          builder ++= s"${"  " * depth}- missing ${prettyPrintType(typ)}$msgEnding\n"
          buildMessage(innerReasons, depth + 1)
      }
    }

    buildMessage(failedDerivations.map(_.reverse).reverse.distinct, 1)
    setImplicitNotFound(builder.toString)
  }

  // since we are inside a whitebox implicit macro, error messages from `c.abort` as not printed. A trick must be used
  // to make the compiler print our custom message. That's done by setting a @implicitNotFound annotation on our
  // `Derivation` class (idea taken from shapeless `Lazy`).
  private[this] def setImplicitNotFound(msg: String): Unit = {
    import c.internal.decorators._
    val infTree = c.typecheck(q"""new _root_.scala.annotation.implicitNotFound($msg)""", silent = false)
    typeOf[Derivation[_]].typeSymbol.setAnnotations(Annotation(infTree))
  }

  private[this] def prettyPrintType(typ: Type): String =
    prettyPrintType(typ.toTree)

  private[this] def prettyPrintType(typ: Tree): String = typ match {
    case tq"Lazy[$lzyArg]" => prettyPrintType(lzyArg) // ignore `Lazy` for the purposes of showing the implicit chain
    case tq"$typeclass[$arg]" => s"a $typeclass instance for type $arg"
    case _ => s"an implicit value of $typ"
  }
}

private object DerivationMacros {

  // Stores the list of paths (lists of types) to failed derivations. Both the outer list and the paths are stored in
  // reverse for efficiency. The type of the `Type` values is path-dependent on the context of the derivation macro and
  // should be able to be cast safely. Before a root derivation returns this list is emptied.
  var failedDerivations: List[List[_ <: Universe#Type]] = Nil

  // Stores the current path of the derivation.
  var currentPath: List[_ <: Universe#Type] = Nil
}
