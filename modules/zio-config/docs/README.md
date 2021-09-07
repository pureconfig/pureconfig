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

val configOpt = ConfigRenderOptions.defaults.setOriginComments(false)

case class Person(name: String, age: Int, children: List[Person])
object Person {
  implicit val confDesc: ConfigDescriptor[Person] =
    (string("name") |@| int("age") |@| list("children")(confDesc))(Person.apply, Person.unapply)
}
```

You can now read and write `Person` without re-implementing or re-deriving `ConfigConvert`.
```scala mdoc
val bob = Person("bob", 10, Nil)
val alice = Person("alice", 42, bob :: Nil)

val res = ConfigWriter[Person].to(alice).render(configOpt)

val maybeAlice = ConfigSource.string(res).load[Person]
```

Writing to HOCON typically does not fail but `zio-config` allows for write failures
as `ConfigDescriptor` transformation allows for failures bidirectionally.
For example, we could have a new `ConfigDescriptor[Person]` that fails to write a `Person`
if they do not qualify as a child.
```scala mdoc:silent
import scala.util.Try
import zio.config.typesafe._
 
val childConfDesc: ConfigDescriptor[Person] =
  Person.confDesc.transformOrFailRight(identity, {
    case Person(_, age, _) if age > 18 => Left("Too old to be a child.")
    case Person(_, _, children) if children.nonEmpty => Left("Child cannot have children.")
    case p => Right(p)
  })

val childConfigConvert: ConfigConvert[Person] = zioConfigConvert(childConfDesc)
```

Note that in such a case an exception will be thrown since writing is not expected to return a failure in `pureconfig`.
```scala mdoc
//writing with zio-config
val zioConfigBob = bob.toHoconString(childConfDesc)
val zioConfigAlice = alice.toHoconString(childConfDesc)

//writing with pureconfig
val pureconfigBob = Try {
  childConfigConvert.to(bob).render(configOpt)
}
val pureconfigAlice = Try {
  childConfigConvert.to(alice).render(configOpt)
}
```
