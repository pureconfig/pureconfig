# ZIO Config module for PureConfig

Support for providing instances of `ConfigReader` given instances of [ZIO Config](https://zio.github.io/zio-config/) `Config`.

## Add pureconfig-zio-config to your project

In addition to the [PureConfig core](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-zio-config" % "0.17.10"
```

## Example

`Person` already has a `Config` instance.
By importing `pureconfig.module.zioconfig._`, it would also have a `ConfigReader` instance.

```scala
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.module.zioconfig._
import zio.config._
import zio.Config
import zio.Config._

val configOpt = ConfigRenderOptions.defaults.setOriginComments(false)

case class Person(name: String, age: Int, children: List[Person])
object Person {
  implicit val confDesc: Config[Person] =
    (string("name") zip int("age") zip listOf("children", confDesc)).to[Person]
}
```

You can now read `Person` without re-implementing or re-deriving `ConfigReader`.

```scala
val alice = ConfigSource.string(
  """
  |name = "alice"
  |age = 42
  |children = [{
  |  name = "bob"
  |  age = 10
  |  children = []
  |}]
  """.stripMargin
).load[Person]
// alice: ConfigReader.Result[Person] = Right(
//   value = Person(
//     name = "alice",
//     age = 42,
//     children = List(Person(name = "bob", age = 10, children = List()))
//   )
// )
```
