package pureconfig.module.magnolia

import magnolia.Magnolia
import pureconfig.Exported

import scala.language.higherKinds
import scala.reflect.macros.{ blackbox, whitebox }

// Wrap the output of Magnolia in an Exported to force it to a lower priority.
// This seems to work, despite magnolia hardcode checks for `macroApplication` symbol
// and relying on getting an diverging implicit expansion error for auto-mode.
// Thankfully at least it doesn't check the output type of its `macroApplication`
object ExportedMagnolia {
  def exportedMagnolia[TC[_], A](c: blackbox.Context)(implicit t: c.WeakTypeTag[A]): c.Expr[Exported[TC[A]]] = {
    val magnoliaTree = forcedBlackboxMagnolia[TC, A](c)
    c.universe.reify(Exported(magnoliaTree.splice))
  }

  // if i TELL you it's whitebox, you BETTER believe me
  def forcedBlackboxMagnolia[TC[_], A](c: blackbox.Context)(implicit t: c.WeakTypeTag[A]): c.Expr[TC[A]] = {
    val c0 = c.asInstanceOf[whitebox.Context]
    c.Expr[TC[A]](Magnolia.gen[A](c.asInstanceOf[c0.type])(t.asInstanceOf[c0.WeakTypeTag[A]]).asInstanceOf[c.Tree])
  }
}
