# Scalaz module for PureConfig

Adds support for selected [scalaz](https://github.com/scalaz/scalaz) data structures to PureConfig, provides instances of
`scalaz` type classes for `ConfigReader`, `ConfigReaderFailures`, `ConfigWriter` and `ConfigConvert` and some syntactic sugar for pureconfig
classes.

## Add pureconfig-scalaz to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-scalaz" % "0.12.1"
```

## Example

### Reading `scalaz` data structures from a config

The following `scalaz` data structures are supported:

* `IList`, `ISet`, `Maybe`, `NonEmptyList` and `==>>`
* `Order[A]` should also be in scope, when you're relying on either `ConfigReader[A ==>> B]` or `ConfigReader[ISet[A]]`.
For example, if your `ISet` instance contains `String` values then `Order[String]` can be imported via `scalaz.std.string._`

Here is an usage example:

```scala
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.scalaz._
import scalaz.{==>>, IList, ISet, Maybe, NonEmptyList}
import scalaz.std.anyVal.intInstance
import scalaz.std.string._

case class ScalazConfig(
  numberLst: IList[Int],
  numberSet: ISet[Int],
  numberNel: NonEmptyList[Int],
  numberMap: String ==>> Int,
  numberMaybe: Maybe[Int]
)
```

We can read a `ScalazConfig` like:
```scala
val scalazConf = parseString("""{
  number-lst: [1,2,3],
  number-set: [1,2,3],
  number-nel: [1,2,3],
  number-map { "one": 1, "two": 2, "three": 3 },
  number-maybe: 1
}""")
// scalazConf: com.typesafe.config.Config = Config(SimpleConfigObject({"number-lst":[1,2,3],"number-map":{"one":1,"three":3,"two":2},"number-maybe":1,"number-nel":[1,2,3],"number-set":[1,2,3]}))

ConfigSource.fromConfig(scalazConf).load[ScalazConfig]
// res0: pureconfig.ConfigReader.Result[ScalazConfig] = Right(ScalazConfig([1,2,3],Bin(2,Bin(1,Tip(),Tip()),Bin(3,Tip(),Tip())),NonEmpty[1,2,3],Bin(three,3,Bin(one,1,Tip,Tip),Bin(two,2,Tip,Tip)),Just(1)))
```

### Using `scalaz` type class instances for readers

In order to put in scope `scalaz` type classes for our readers and extend them with the extra
operations provided by `scalaz`, we need some extra imports:

```scala
import pureconfig.module.scalaz.instances._
import scalaz._
import scalaz.Scalaz._
```

We are now ready to use the new syntax:

```scala
case class SimpleConfig(i: Int)

// a reader that always returns SimpleConfig(42)
val constReader = SimpleConfig(42).point[ConfigReader]

// a reader that returns SimpleConfig(-1) if an error occurs
val safeReader = ConfigReader[SimpleConfig].handleError(_ => SimpleConfig(-1).point[ConfigReader])
```

And we can finally put them to use:

```scala
val validConf = parseString("""{ i: 1 }""")
// validConf: com.typesafe.config.Config = Config(SimpleConfigObject({"i":1}))

val invalidConf = parseString("""{ s: "abc" }""")
// invalidConf: com.typesafe.config.Config = Config(SimpleConfigObject({"s":"abc"}))

constReader.from(validConf.root())
// res3: pureconfig.ConfigReader.Result[SimpleConfig] = Right(SimpleConfig(42))

constReader.from(invalidConf.root())
// res4: pureconfig.ConfigReader.Result[SimpleConfig] = Right(SimpleConfig(42))

safeReader.from(validConf.root())
// res5: pureconfig.ConfigReader.Result[SimpleConfig] = Right(SimpleConfig(1))

safeReader.from(invalidConf.root())
// res6: pureconfig.ConfigReader.Result[SimpleConfig] = Right(SimpleConfig(-1))
```

In case there's a necessity to parse multiple configs and accumulate errors, you could leverage from `Semigroup` instance for `ConfigReaderFailures`:

```scala
val anotherInvalidConf = parseString("""{ i: false }""")
// anotherInvalidConf: com.typesafe.config.Config = Config(SimpleConfigObject({"i":false}))

List(validConf, invalidConf, anotherInvalidConf).traverseU { c =>
  Validation.fromEither(implicitly[ConfigReader[SimpleConfig]].from(c.root))
}
// res7: scalaz.Validation[pureconfig.error.ConfigReaderFailures,List[SimpleConfig]] = Failure(ConfigReaderFailures(ConvertFailure(KeyNotFound(i,Set()),None,),List(ConvertFailure(WrongType(BOOLEAN,Set(NUMBER)),None,i))))
```

### Extra syntactic sugar

We can provide some useful extension methods by importing:

```scala
import pureconfig.module.scalaz.syntax._
```

For example, you can easily convert a `ConfigReaderFailures` to a `NonEmptyList[ConfigReaderFailure]`:

```scala
case class MyConfig(i: Int, s: String)
```
```scala
val myConf = parseString("{}")
// myConf: com.typesafe.config.Config = Config(SimpleConfigObject({}))

val res = ConfigSource.fromConfig(myConf).load[MyConfig].left.map(_.toNel)
// res: scala.util.Either[scalaz.NonEmptyList[pureconfig.error.ConfigReaderFailure],MyConfig] = Left(NonEmpty[ConvertFailure(KeyNotFound(i,Set()),None,),ConvertFailure(KeyNotFound(s,Set()),None,)])
```

This allows `scalaz` users to easily convert a result of a `ConfigReader` into a `ValidatedNel`:

```scala
import scalaz.{ Validation, ValidationNel }
import pureconfig.error._
```

```scala
val result: ValidationNel[ConfigReaderFailure, MyConfig] =
  Validation.fromEither(res)
// result: scalaz.ValidationNel[pureconfig.error.ConfigReaderFailure,MyConfig] = Failure(NonEmpty[ConvertFailure(KeyNotFound(i,Set()),None,),ConvertFailure(KeyNotFound(s,Set()),None,)])
```

Also, you could create `ConfigReader`s using `scalaz` types:

```scala
case class Tweet(msg: String)

val tweetReader: ConfigReader[Tweet] = ConfigReader.fromNonEmptyStringDisjunction { s =>
  if (s.length <= 140) Tweet(s).right
  else (new FailureReason { def description: String = "Too long to be a tweet!" }).left
}
```
