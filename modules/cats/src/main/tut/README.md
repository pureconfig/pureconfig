# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig, provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert` and some syntactic sugar for pureconfig
classes.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.9.1"
```

## Example

### Reading cats data structures from a config

The following cats data structures are supported: 

* `NonEmptyList`, `NonEmptyVector`, `NonEmptySet`
* `NonEmptyMap[K, V]` implicits of `ConfigReader[Map[K, V]]` and `Order[K]` should be in the scope.
For example, if your key is a `String` then `Order[String]` can be imported from `cats.instances.string._`

All of these data structures rely on the instances of their unrestricted (i.e. possibly empty) variants.
Custom collection readers, if any, may affect the behavior of these too.

Here is an example of usage:

```tut:silent
import cats.data.{NonEmptyList, NonEmptySet, NonEmptyVector, NonEmptyMap}
import cats.instances.string._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.cats._

case class MyConfig(
  numberList: NonEmptyList[Int],
  numberSet: NonEmptySet[Int],
  numberVector: NonEmptyVector[Int],
  numberMap: NonEmptyMap[String, Int]
)
```

We can read a `MyConfig` like:
```tut:book
val conf = parseString("""{ 
  number-list: [1,2,3],
  number-set: [1,2,3],
  number-vector: [1,2,3],
  number-map { "one": 1, "two": 2, "three": 3 }     
}""")

loadConfig[MyConfig](conf)
```

### Using cats type class instances for readers and writers

In order to to put in scope the `cats` type classes for our readers and writers and extend the latter with the extra
operations provided by `cats`, we need some extra imports:

```tut:silent
import cats._
import cats.syntax.all._
import pureconfig.module.cats.instances._
```

We are now ready to use the new syntax:

```tut:silent
// a reader that returns a constant value
val constIntReader = 42.pure[ConfigReader]

// a Int reader that returns -1 if an error occurs
val safeIntReader = ConfigReader[Int].handleError(_ => -1)

// a writer for `Some[A]` (note that the `ConfigWriter` trait is invariant)
implicit def someWriter[A: ConfigWriter] = ConfigWriter[Option[A]].narrow[Some[A]]
```

And we can finally put them to use:

```tut:book
constIntReader.from(conf.root())

safeIntReader.from(conf.root())

someWriter[String].to(Some("abc"))
```

### Extra syntatic sugar

We can provide some useful extension methods by importing:

```tut:silent
import pureconfig.module.cats.syntax._
```

For example, you can easily convert a `ConfigReaderFailures` to a `NonEmptyList[ConfigReaderFailure]`:

```tut:book
case class MyConfig2(a: Int, b: String)

val conf = parseString("{}")
val res = loadConfig[MyConfig2](conf).left.map(_.toNonEmptyList)
```

This allows cats users to easily convert a result of a `ConfigReader` into a `ValidatedNel`:

```tut:silent
import cats.data.{ Validated, ValidatedNel }
import pureconfig.error.ConfigReaderFailure
```

```tut:book
val catsRes: ValidatedNel[ConfigReaderFailure, MyConfig2] =
  Validated.fromEither(res)
```
