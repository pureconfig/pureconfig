# Circe module for PureConfig

Adds support for [Circe](https://circe.github.io/circe/) `Json` to PureConfig.

## Add pureconfig-circe to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-circe" % "0.11.0"
```

## Example

Imports to be used below:

```tut:silent
import io.circe._
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.circe._
```

### Reading Json values directly

Imagine a class, `UserConfig`, that has a `Json` field:

```tut:silent
case class UserConfig(username: String, age: Int, custom: Json)
```

A `UserConfig` can be read like this:

```tut:book
val conf = ConfigFactory.parseString("""{
  username = nathan
  age = 31
  custom = {
    favoriteFood = pizza
  }
}""")

loadConfig[UserConfig](conf)
```

