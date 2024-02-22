# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig, provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert` and some syntactic sugar for pureconfig
classes.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.17.6"
```

## Example

### Reading cats data structures from a config

The following cats data structures are supported: 

* Cats data types with `Foldable` and `Alternative` (i.e. non-reducible) typeclass instances, e.g. `Chain`.
* `NonEmptyList`, `NonEmptyVector`, `NonEmptySet`, `NonEmptyChain`
* `NonEmptyMap[K, V]` implicits of `ConfigReader[Map[K, V]]` and `Order[K]` should be in the scope.
For example, if your key is a `String` then `Order[String]` can be imported from `cats.instances.string._`

All of these data structures rely on the instances of their unrestricted (i.e. possibly empty) variants.
Custom collection readers, if any, may affect the behavior of these too.

Here is an example of usage:

```scala
import cats.data.{NonEmptyList, NonEmptySet, NonEmptyVector, NonEmptyMap, NonEmptyChain}
import cats.instances.string._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.cats._

case class MyConfig(
  numberList: NonEmptyList[Int],
  numberSet: NonEmptySet[Int],
  numberVector: NonEmptyVector[Int],
  numberMap: NonEmptyMap[String, Int],
  numberChain: NonEmptyChain[Int]
)
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{
  number-list: [1,2,3],
  number-set: [1,2,3],
  number-vector: [1,2,3],
  number-map { "one": 1, "two": 2, "three": 3 },
  number-chain: [1,2,3]
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"number-chain":[1,2,3],"number-list":[1,2,3],"number-map":{"one":1,"three":3,"two":2},"number-set":[1,2,3],"number-vector":[1,2,3]}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(
//   MyConfig(
//     NonEmptyList(1, List(2, 3)),
//     TreeSet(1, 2, 3),
//     NonEmptyVector(1, 2, 3),
//     Map("one" -> 1, "three" -> 3, "two" -> 2),
//     Append(Singleton(1), Append(Singleton(2), Singleton(3)))
//   )
// )
```

### Using cats type class instances for readers and writers

In order to put in scope the `cats` type classes for our readers and writers and extend the latter with the extra
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
// res1: ConfigReader.Result[Int] = Right(42)

safeIntReader.from(conf.root())
// res2: ConfigReader.Result[Int] = Right(-1)

someWriter[String].to(Some("abc"))
// res3: com.typesafe.config.ConfigValue = Quoted("abc")
```

### Extra syntactic sugar

We can provide some useful extension methods by importing:

```scala
import pureconfig.module.cats.syntax._
```

For example, you can easily convert a `ConfigReaderFailures` to a `NonEmptyList[ConfigReaderFailure]`:

```scala
case class MyConfig2(a: Int, b: String)

val conf2 = parseString("{}")
// conf2: com.typesafe.config.Config = Config(SimpleConfigObject({}))

val res = ConfigSource.fromConfig(conf2).load[MyConfig2].left.map(_.toNonEmptyList)
// res: Either[NonEmptyList[error.ConfigReaderFailure], MyConfig2] = Left(
//   NonEmptyList(
//     ConvertFailure(KeyNotFound("a", Set()), Some(ConfigOrigin(String)), ""),
//     List(
//       ConvertFailure(KeyNotFound("b", Set()), Some(ConfigOrigin(String)), "")
//     )
//   )
// )
```

This allows cats users to easily convert a result of a `ConfigReader` into a `ValidatedNel`:

```scala
import cats.data.{ Validated, ValidatedNel }
import pureconfig.error.ConfigReaderFailure
```

```scala
val catsRes: ValidatedNel[ConfigReaderFailure, MyConfig2] =
  Validated.fromEither(res)
// catsRes: ValidatedNel[ConfigReaderFailure, MyConfig2] = Invalid(
//   NonEmptyList(
//     ConvertFailure(KeyNotFound("a", Set()), Some(ConfigOrigin(String)), ""),
//     List(
//       ConvertFailure(KeyNotFound("b", Set()), Some(ConfigOrigin(String)), "")
//     )
//   )
// )
```
