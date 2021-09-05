# ZIO Config module for PureConfig

Support for providing instances of `ConfigCovert` given instances of ZIO Config `ConfigDescriptor`

## Add pureconfig-zio-config to your project

In addition to the [PureConfig core](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-zio-config" % "0.16.0"
```

## Example

`Person` already has a `ConfigDescriptor` instance.
By importing `pureconfig.module.zioconfig._`, it would also have a `ConfigConvert` instance.

```scala
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.module.zioconfig._
import zio.config.ConfigDescriptor
import zio.config.magnolia.DeriveConfigDescriptor.descriptor

case class Person(name: String, age: Int, children: List[Person])
object Person { implicit val confDesc: ConfigDescriptor[Person] = descriptor }
```

You can now read and write `Person` without re-implementing or re-deriving `ConfigConvert`.
```scala
val alice = Person("alice", 42, Person("bob", 24, Nil) :: Nil)
// alice: Person = Person("alice", 42, List(Person("bob", 24, List())))

val res = ConfigWriter[Person].to(alice)
  .render(ConfigRenderOptions.defaults.setOriginComments(false))
// res: String = """{
//     "age" : "42",
//     "children" : [
//         {
//             "age" : "24",
//             "children" : [],
//             "name" : "bob"
//         }
//     ],
//     "name" : "alice"
// }
// """

val maybeAlice = ConfigSource.string(res).load[Person]
// maybeAlice: ConfigReader.Result[Person] = Right(
//   Person("alice", 42, List(Person("bob", 24, List())))
// )
```

