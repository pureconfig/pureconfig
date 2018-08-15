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
* `NonEmptyMap[K, V]` only string keys are supported by default because it relies on `Map`.
For a custom key you'll also have to provide an implicit on Ordering[K].

Here is an example of usage:

```scala
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
```scala
val conf = parseString("""{ 
  number-list: [1,2,3],
  number-set: [1,2,3],
  number-vector: [1,2,3],
  number-map { "one": 1, "two": 2, "three": 3 }     
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"number-list":[1,2,3],"number-map":{"one":1,"three":3,"two":2},"number-set":[1,2,3],"number-vector":[1,2,3]}))

loadConfig[MyConfig](conf)
// res0: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(NonEmptyList(1, 2, 3),TreeSet(1, 2, 3),NonEmptyVector(1, 2, 3),Map(one -> 1, three -> 3, two -> 2)))
```

Note that `NonEmptyMap[K,V]` requires an implicit of `Order[K]`. If your key is a `String` you should import `cats.instances.string._`.

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
val constIntReader = 42.pure[ConfigReader]

// a Int reader that returns -1 if an error occurs
val safeIntReader = ConfigReader[Int].handleError(_ => -1)

// a writer for `Some[A]` (note that the `ConfigWriter` trait is invariant)
implicit def someWriter[A: ConfigWriter] = ConfigWriter[Option[A]].narrow[Some[A]]
```

And we can finally put them to use:

```scala
constIntReader.from(conf.root())
// res4: Either[pureconfig.error.ConfigReaderFailures,Int] = Right(42)

safeIntReader.from(conf.root())
// res5: Either[pureconfig.error.ConfigReaderFailures,Int] = Right(-1)

someWriter[String].to(Some("abc"))
// res6: com.typesafe.config.ConfigValue = Quoted("abc")
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
