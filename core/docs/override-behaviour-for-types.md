## Override behaviour for types

It is possible to override the behaviour of PureConfig for a certain type by
implementing another instance of the `ConfigConvert` type class. For instance,
the default behaviour of PureConfig for `String` is to return the string itself
in the configuration:

```scala
import com.typesafe.config.ConfigValueFactory
import pureconfig._

ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
// returns Right("FooBar")
```

Now let's say that we want to override this behaviour such that `String`s are
always read lower case. We can do:

```scala
import com.typesafe.config.ConfigValueFactory
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ fromStringReader, catchReadError }

implicit val overrideStrConvert = fromStringReader(catchReadError(_.toLowerCase))

ConfigConvert[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
// returns Right("foobar")
```
