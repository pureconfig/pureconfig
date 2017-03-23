## Handling missing keys

The default behavior of `ConfigConvert`s that are derived in PureConfig is to
raise a `KeyNotFoundException` when a required key is missing. The only
exception is the `Option[_]` type, which is read as `None` when a key is
missing.

Consider this configuration:

```scala
import com.typesafe.config._
import pureconfig._
import pureconfig.syntax._

case class Foo(a: Int)
case class FooOpt(a: Option[Int])
```

Loading a `Foo` results in a `Left` because of missing keys, but loading a `FooOpt` produces a `Right`:

```scala
ConfigFactory.empty.to[Foo]
// res1: Either[pureconfig.error.ConfigReaderFailures,Foo] = Left(ConfigReaderFailures(KeyNotFound(a,None),List()))

ConfigFactory.empty.to[FooOpt]
// res2: Either[pureconfig.error.ConfigReaderFailures,FooOpt] = Right(FooOpt(None))
```

However, if you want to allow your custom `ConfigConvert`s to handle missing
keys, you can extend the `AllowMissingKey` trait. For `ConfigConvert`s extending
`AllowMissingKey`, a missing key will issue a call to the `from` method of the
available `ConfigConvert` for that type with a `null` value.

Under this setup:

```scala
import com.typesafe.config._
import pureconfig.syntax._

implicit val cc = new ConfigConvert[Int] with AllowMissingKey {
  override def from(config: ConfigValue) =
    if (config == null) Right(42) else config.to[Int]

  override def to(t: Int): ConfigValue = ConfigValueFactory.fromAnyRef(t)
}
```

You can load an empty configuration and get a `Right`:

```scala
ConfigFactory.empty.to[Foo]
// res4: Either[pureconfig.error.ConfigReaderFailures,Foo] = Right(Foo(42))
```
