---
layout: docs
title: Case classes
---
## Override behaviour for case classes

PureConfig has to assume some conventions and behaviours when deriving
`ConfigConvert` instances for case classes:

- How do keys in config objects map to field names of the case class?
- Are unknown keys allowed in the config object?
- Should default values in case class fields be applied when its respective
  config key is missing?

By default, PureConfig:

- expects config keys to be written in kebab case (such as `my-field`) and the
associated field names are written in camel case (such as `myField`);
- allows unknown keys;
- uses the default values when a key is missing.

All of these assumptions can be overridden by putting an implicit
`ProductHint` - an object that "hints" PureConfig on how to best derive
converters for products in scope.

### Field mappings

In case the naming convention you use in your configuration files differs from
the default one, PureConfig allows you to define proper mappings. A mapping
between different naming conventions is done using a `ConfigFieldMapping`
object, with which one can construct a `ProductHint`. The `ConfigFieldMapping`
trait has a single `apply` method that maps field names in Scala objects to
field names in the source configuration file. 

For instance, here's a contrived
example where the configuration file has all keys in upper case and we're
loading it into a type whose fields are all in lower case. First, define a `ProductHint`
instance in implict scope:

```tut:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._

case class SampleConf(foo: Int, bar: String)

implicit val productHint = ProductHint[SampleConf](new ConfigFieldMapping {
  def apply(fieldName: String) = fieldName.toUpperCase
})
```

Then load a config:
```tut:book
val conf = parseString("""{
  FOO = 2
  BAR = "two"
}""")

loadConfig[SampleConf](conf)
```

PureConfig provides a way to create a `ConfigFieldMapping` by defining the
naming conventions of the fields in the Scala object and in the configuration
file. Some of the most used naming conventions are supported directly in the
library:

* [`CamelCase`](https://en.wikipedia.org/wiki/Camel_case): `camelCase`, `useMorePureconfig`
* [`SnakeCase`](https://en.wikipedia.org/wiki/Snake_case): `snake_case`, `use_more_pureconfig`
* [`KebabCase`](http://wiki.c2.com/?KebabCase): `kebab-case`, `use-more-pureconfig`
* [`PascalCase`](https://en.wikipedia.org/wiki/PascalCase): `PascalCase`, `UseMorePureconfig`

You can use the `apply` method of `ConfigFieldMapping` that accepts the two
naming conventions (for the fields in the Scala object and for the fields in the
configuration file, respectively). A common use case is to have both your field
names and your configuration files in `camelCase`. In order to support it, you
can make sure the following implicit is in scope before loading or writing
configuration files:

```tut:silent
import pureconfig._

implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
```

### Default field values

If a case class has a default argument and the underlying configuration is
missing a value for that field, then by default PureConfig will happily
create an instance of the class, loading the other values from the
configuration.

For example, with this setup:

```tut:reset:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import scala.concurrent.duration._

case class Holiday(where: String = "last resort", howLong: Duration = 7 days)
```

We can load configurations using default values:
```tut:book
// Defaulting `where`
loadConfig[Holiday](parseString("""{ how-long: 21 days }"""))

// Defaulting `howLong`
loadConfig[Holiday](parseString("""{ where: Zürich }"""))

// Defaulting both arguments
loadConfig[Holiday](parseString("""{}"""))

// Specifying both arguments
loadConfig[Holiday](parseString("""{ where: Texas, how-long: 3 hours }"""))
```

A `ProductHint` can make the conversion fail if a key is missing from the
config regardless of whether a default value exists or not:

```tut:book
implicit val hint = ProductHint[Holiday](useDefaultArgs = false)

loadConfig[Holiday](parseString("""{ how-long: 21 days }"""))
```

### Unknown keys

By default, PureConfig ignores keys in the config that do not map to any
case class field, leading to potential bugs due to misspellings:

```tut:reset:invisible
// reset tut's REPL sesstion to remove the implicit ProductHint defined above.
import pureconfig._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import scala.concurrent.duration._

case class Holiday(where: String = "last resort", howLong: Duration = 7 days)
```
```tut:book
loadConfig[Holiday](parseString("""{ wher: Texas, how-long: 21 days }"""))
```

With a `ProductHint`, one can tell the converter to fail if an unknown key is
found:

```tut:book
implicit val hint = ProductHint[Holiday](allowUnknownKeys = false)

loadConfig[Holiday](parseString("""{ wher: Texas, how-long: 21 days }"""))
```
