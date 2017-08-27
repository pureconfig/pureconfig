## Debugging implicits not found

When we want to load a config with a large structure and Scala refuses to compile a reader for it, it can be difficult
to understand the reason of the failure. Consider the following example:

```scala
import com.typesafe.config.ConfigFactory
import pureconfig.syntax._

class Custom(x: Int, s: String) // not a case class
class Custom2(x: Int, s: String) // not a case class

sealed trait Conf
case class ConfA(a: Boolean, b: Option[Boolean]) extends Conf
sealed trait ConfB extends Conf
case class ConfB1(a: Int) extends ConfB
case class ConfB2(a: String) extends ConfB
case class ConfC(a: Option[Custom], b: Custom2) extends Conf
```

When we try to load a `Conf` from a config, we'll simply get this error message:

```scala
ConfigFactory.load.to[Conf]
// <console>:18: error: could not find implicit value for parameter reader: pureconfig.Derivation[pureconfig.ConfigReader[Conf]]
//        ConfigFactory.load.to[Conf]
//                             ^
```

In PureConfig, the derivation of config readers and writers is done by chaining implicits - the converters of larger
structures (like `Conf`) depend on the implicit converters of smaller ones (like `Boolean` or `Custom`). However, Scala
is not helpful in case one of those upstream dependencies is missing, limiting itself to showing the message above.

Since version 0.8.0, PureConfig provides a way to obtain better error messages for those cases. To enable it, we need to
add the following entry to our SBT project:

```scala
scalacOptions += "-Xmacro-settings:materialize-derivations"
```

When code using PureConfig derived converters is compiled using the compiler flag above, an internal macro will analyze
the dependencies between converters and show a much more helpful message in case of an error:

```scala
ConfigFactory.load.to[Conf]
// <console>:18: error: could not derive a ConfigReader instance for type Conf, because:
//   - missing a ConfigReader instance for type ConfC, because:
//     - missing a ConfigReader instance for type Option[Custom], because:
//       - missing a ConfigReader instance for type Custom
//     - missing a ConfigReader instance for type Custom2
//        ConfigFactory.load.to[Conf]
//                             ^
```

Since this is an experimental feature, it may not work as intended in some unforesseen cases. If you find any issue with
it, please do open an issue!
