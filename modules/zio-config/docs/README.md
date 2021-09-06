# ZIO Config module for PureConfig

Support for providing instances of `ConfigCovert` given instances of [ZIO Config](https://zio.github.io/zio-config/) `ConfigDescriptor`.

## Add pureconfig-zio-config to your project

In addition to the [PureConfig core](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-zio-config" % "@VERSION@"
```

## Example

`Person` already has a `ConfigDescriptor` instance.
By importing `pureconfig.module.zioconfig._`, it would also have a `ConfigConvert` instance.

```scala mdoc:silent
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.module.zioconfig._
import zio.config.ConfigDescriptor
import zio.config.ConfigDescriptor._

case class Person(name: String, age: Int, children: List[Person])
object Person {
  implicit val confDesc: ConfigDescriptor[Person] =
    (string("name") |@| int("age") |@| list("children")(confDesc))(Person.apply, Person.unapply)
}
```

You can now read and write `Person` without re-implementing or re-deriving `ConfigConvert`.
```scala mdoc
val alice = Person("alice", 42, Person("bob", 24, Nil) :: Nil)

val res = ConfigWriter[Person].to(alice)
  .render(ConfigRenderOptions.defaults.setOriginComments(false))

val maybeAlice = ConfigSource.string(res).load[Person]
```
