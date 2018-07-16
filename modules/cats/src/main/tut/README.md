# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig, provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert` and some syntatic sugar for pureconfig
classes.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.9.1"
```

## Example

### Reading cats data structures from a config

To load a `NonEmptyList[Int]` into a configuration, we need a class to hold our configuration:

```tut:silent
import cats.data.{NonEmptyList, NonEmptySet, NonEmptyVector}
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.cats._

case class MyConfig(numbers: NonEmptyList[Int])
```

We can read a `MyConfig` like:
```tut:book
val conf = parseString("""{ numbers: [1,2,3] }""")
loadConfig[MyConfig](conf)
```

You can also load `NonEmptyVector`. First, define a case class for the config:

```tut:silent
case class MyVecConfig(numbers: NonEmptyVector[Int])
```

then load the config:
```tut:book
loadConfig[MyVecConfig](conf)
```

Similarly, `NonEmptySet` is also supported:

```tut:silent
case class MySetConfig(numbers: NonEmptySet[Int])
```
```tut:book
loadConfig[MySetConfig](conf)
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
val constIntReader = Monad[ConfigReader].pure(42)

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
