# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig, provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert` and some syntatic sugar for pureconfig
classes.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.9.0"
```

## Example

### Reading cats data structures from a config

To load a `NonEmptyList[Int]` into a configuration, we need a class to hold our configuration:

```scala
import cats.data.{NonEmptyList, NonEmptyVector}
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.cats._

case class MyConfig(numbers: NonEmptyList[Int])
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{ numbers: [1,2,3] }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"numbers":[1,2,3]}))

loadConfig[MyConfig](conf)
// res1: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(NonEmptyList(1, 2, 3)))
```

You can also load `NonEmptyVector`. First, define a case class for the config:

```scala
case class MyVecConfig(numbers: NonEmptyVector[Int])
```

then load the config:
```scala
loadConfig[MyVecConfig](conf)
// res2: Either[pureconfig.error.ConfigReaderFailures,MyVecConfig] = Right(MyVecConfig(NonEmptyVector(1, 2, 3)))
```

### Using cats type class instances for readers and writers

In order to to put in scope the `cats` type classes for our readers and writers and extend the latter with the extra
operations provided by `cats`, we need some extra imports:

```scala
import cats._
import cats.syntax.all._
import pureconfig.module.cats.instances._
```

We are now ready to use the new syntax:

```scala
// a reader that returns a constant value
val constIntReader = Monad[ConfigReader].pure(42)

// a Int reader that returns -1 if an error occurs
val safeIntReader = ConfigReader[Int].handleError(_ => -1)

// a writer for `Some[A]` (note that the `ConfigWriter` trait is invariant)
implicit def someWriter[A: ConfigWriter] = ConfigWriter[Option[A]].narrow[Some[A]]
```

And we can finally put them to use:

```scala
constIntReader.from(conf.root())
// res8: Either[pureconfig.error.ConfigReaderFailures,Int] = Right(42)

safeIntReader.from(conf.root())
// res9: Either[pureconfig.error.ConfigReaderFailures,Int] = Right(-1)

someWriter[String].to(Some("abc"))
// res10: com.typesafe.config.ConfigValue = Quoted("abc")
```

### Extra syntatic sugar

We can provide some useful extension methods by importing:

```scala
import pureconfig.module.cats.syntax._
```

For example, you can easily convert a `ConfigReaderFailures` to a `NonEmptyList[ConfigReaderFailure]`:

```scala
case class MyConfig2(a: Int, b: String)
// defined class MyConfig2

val conf = parseString("{}")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({}))

val res = loadConfig[MyConfig2](conf).left.map(_.toNonEmptyList)
// res: scala.util.Either[cats.data.NonEmptyList[pureconfig.error.ConfigReaderFailure],MyConfig2] = Left(NonEmptyList(ConvertFailure(KeyNotFound(a,Set()),None,), ConvertFailure(KeyNotFound(b,Set()),None,)))
```

This allows cats users to easily convert a result of a `ConfigReader` into a `ValidatedNel`:

```scala
import cats.data.{ Validated, ValidatedNel }
import pureconfig.error.ConfigReaderFailure
```

```scala
val catsRes: ValidatedNel[ConfigReaderFailure, MyConfig2] =
  Validated.fromEither(res)
// catsRes: cats.data.ValidatedNel[pureconfig.error.ConfigReaderFailure,MyConfig2] = Invalid(NonEmptyList(ConvertFailure(KeyNotFound(a,Set()),None,), ConvertFailure(KeyNotFound(b,Set()),None,)))
```
