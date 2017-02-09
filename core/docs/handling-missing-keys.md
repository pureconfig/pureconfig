## Handling missing keys

The default behavior of `ConfigConvert`s that are derived in PureConfig is to
raise a `KeyNotFoundException` when a required key is missing. The only
exception is the `Option[_]` type, which is read as `None` when a key is
missing:

```scala
import com.typesafe.config._
import pureconfig._
import pureconfig.syntax._

case class Foo(a: Int)
case class FooOpt(a: Option[Int])

ConfigFactory.empty.to[Foo]
// returns Failure(KeyNotFoundException("a"))

ConfigFactory.empty.to[FooOpt]
// returns Success(FooOpt(None))
```

However, if you want to allow your custom `ConfigConvert`s to handle missing
keys, you can extend the `AllowMissingKey` trait. For `ConfigConvert`s extending
`AllowMissingKey`, a missing key will issue a call to the `from` method of the
available `ConfigConvert` for that type with a `null` value:

```scala
import scala.util.{Success, Try}

implicit val cc = new ConfigConvert[Int] with AllowMissingKey {
  override def from(config: ConfigValue): Try[Int] =
    if (config == null) Success(42) else Try(config.render(ConfigRenderOptions.concise).toInt)

  override def to(t: Int): ConfigValue = ConfigValueFactory.fromAnyRef(t)
}

ConfigFactory.empty.to[Foo]
// returns Success(Foo(42))
```
