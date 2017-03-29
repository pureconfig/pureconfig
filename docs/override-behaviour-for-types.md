## Override behaviour for types

It is possible to override the behaviour of PureConfig for a certain type by
implementing another instance of the `ConfigConvert` type class. For instance,
the default behaviour of PureConfig for `String` is to return the string itself
in the configuration.

When the default type class instances are imported:

```scala
import com.typesafe.config.ConfigValueFactory
import pureconfig._
```
PureConfig returns the string itself, "FooBar" in this example:

```scala
ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
// res0: Either[pureconfig.error.ConfigReaderFailures,String] = Right(FooBar)
```

Now let's say that we want to override this behaviour such that `String`s are
always read lower case. We can define a custom `ConfigConvert` instance for `String`:

```scala
import com.typesafe.config.ConfigValueFactory
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ catchReadError, viaString }

implicit val overrideStrConvert = viaString[String](catchReadError(_.toLowerCase), identity)
```

PureConfig will now use the custom `overrideStrConvert` instance:
```scala
ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
// res2: Either[pureconfig.error.ConfigReaderFailures,String] = Right(foobar)
```
