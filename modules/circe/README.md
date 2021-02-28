# Circe module for PureConfig

Adds support for [Circe](https://circe.github.io/circe/) `Json` to PureConfig.

## Add pureconfig-circe to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-circe" % "0.14.1"
```

## Example

Imports to be used below:

```scala
import io.circe._
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.circe._
```

### Reading Json values directly

Imagine a class, `UserConfig`, that has a `Json` field:

```scala
case class UserConfig(username: String, age: Int, custom: Json)
```

A `UserConfig` can be read like this:

```scala
val conf = ConfigFactory.parseString("""{
  username = nathan
  age = 31
  custom = {
    favoriteFood = pizza
  }
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"age":31,"custom":{"favoriteFood":"pizza"},"username":"nathan"}))

ConfigSource.fromConfig(conf).load[UserConfig]
// res0: ConfigReader.Result[UserConfig] = Right(
//   UserConfig("nathan", 31, JObject(object[favoriteFood -> "pizza"]))
// )
```

