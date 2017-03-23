## Override behaviour for types

It is possible to override the behaviour of PureConfig for a certain type by
implementing another instance of the `ConfigConvert` type class. For instance,
the default behaviour of PureConfig for `String` is to return the string itself
in the configuration.

When the default type class instances are imported:

```tut:silent
import com.typesafe.config.ConfigValueFactory
import pureconfig._
```
PureConfig returns the string itself, "FooBar" in this example:

```tut:book
ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```

Now let's say that we want to override this behaviour such that `String`s are
always read lower case. We can define a custom `ConfigConvert` instance for `String`:

```tut:silent
import com.typesafe.config.ConfigValueFactory
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ fromStringReader, catchReadError }

implicit val overrideStrConvert = fromStringReader(catchReadError(_.toLowerCase))
```

PureConfig will now use the custom `overrideStrConvert` instance:
```tut:book
ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```
