# ZIO Config module for PureConfig

Support for providing instances of `ConfigCovert` given instances of [ZIO Config](https://zio.github.io/zio-config/) `ConfigDescriptor`.

## Add pureconfig-zio-config to your project

In addition to the [PureConfig core](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-zio-config" % "0.17.6"
```

## Example

`Person` already has a `ConfigDescriptor` instance.
By importing `pureconfig.module.zioconfig._`, it would also have a `ConfigConvert` instance.
```scala
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
```scala
val bob = Person("bob", 10, Nil)
// bob: Person = Person("bob", 10, List())
val alice = Person("alice", 42, bob :: Nil)
// alice: Person = Person("alice", 42, List(Person("bob", 10, List())))

val res = ConfigWriter[Person].to(alice).render(configOpt)
// res: String = """{
//     "age" : "42",
//     "children" : [
//         {
//             "age" : "10",
//             "children" : [],
//             "name" : "bob"
//         }
//     ],
//     "name" : "alice"
// }
// """

val maybeAlice = ConfigSource.string(res).load[Person]
// maybeAlice: ConfigReader.Result[Person] = Right(
//   Person("alice", 42, List(Person("bob", 10, List())))
// )
```

Writing to HOCON typically does not fail but `zio-config` allows for write failures
as `ConfigDescriptor` transformation allows for failures bidirectionally.
For example, we could have a new `ConfigDescriptor[Person]` that fails to write a `Person`
if they do not qualify as a child.
```scala
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
```scala
//writing with zio-config
val zioConfigBob = bob.toHoconString(childConfDesc)
// zioConfigBob: Either[String, String] = Right(
//   """age="10"
// children=[]
// name=bob
// """
// )
val zioConfigAlice = alice.toHoconString(childConfDesc)
// zioConfigAlice: Either[String, String] = Left("Too old to be a child.")

//writing with pureconfig
val pureconfigBob = Try {
  childConfigConvert.to(bob).render(configOpt)
}
// pureconfigBob: Try[String] = Success(
//   """{
//     "age" : "10",
//     "children" : [],
//     "name" : "bob"
// }
// """
// )
val pureconfigAlice = Try {
  childConfigConvert.to(alice).render(configOpt)
}
// pureconfigAlice: Try[String] = Failure(
//   java.lang.Exception: ZioConfigWriteException: Too old to be a child.
// )
```
