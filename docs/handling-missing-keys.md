## Handling missing keys

The default behavior of `ConfigReader`s that are derived in PureConfig is to
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
// res1: Either[pureconfig.error.ConfigReaderFailures,Foo] = Left(ConfigReaderFailures(KeyNotFound(a,None,Set()),List()))

ConfigFactory.empty.to[FooOpt]
// res2: Either[pureconfig.error.ConfigReaderFailures,FooOpt] = Right(FooOpt(None))
```

However, if you want to allow your custom `ConfigReader`s to handle missing
keys, you can extend the `AllowMissingKey` trait. For `ConfigReader`s extending
`AllowMissingKey`, a missing key will issue a call to the `from` method of the
available `ConfigReader` for that type with a cursor to an undefined value.

Under this setup:

```scala
import com.typesafe.config._
import pureconfig.syntax._

implicit val cc = new ConfigReader[Int] with AllowMissingKey {
  override def from(cur: ConfigCursor) =
    if (cur.isUndefined) Right(42) else cur.to[Int]
}
```

You can load an empty configuration and get a `Right`:

```scala
ConfigFactory.empty.to[Foo]
// res4: Either[pureconfig.error.ConfigReaderFailures,Foo] = Right(Foo(42))
```
