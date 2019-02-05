# Enum module for PureConfig

Adds support for [julienrf's enum](https://github.com/julienrf/enum) library to PureConfig.

## Why

Automatically create a converter to read [enum](https://github.com/julienrf/enum) elements from a configuration.

## Add pureconfig-enum to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-enum" % "0.10.2"
```

## Example

Given a Greeting ADT composed of `case object`s with an `implicit` `Enum` instance:

```scala
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.enum._
import com.typesafe.config.ConfigFactory.parseString
import enum.Enum

sealed trait Greeting

object Greeting {
  case object Hello extends Greeting
  case object WhisperHello extends Greeting
  case object GoodBye extends Greeting
  case object ShoutGoodBye extends Greeting

  final implicit val EnumInstance: Enum[Greeting] = Enum.derived[Greeting]
}
```

And a class to hold the configuration:
```scala
case class GreetingConf(start: Greeting, end: Greeting)
```

We can read a GreetingConf like:
```scala
val conf = parseString("""{
  start: WhisperHello
  end: ShoutGoodBye
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"end":"ShoutGoodBye","start":"WhisperHello"}))

loadConfig[GreetingConf](conf)
// res0: pureconfig.ConfigReader.Result[GreetingConf] = Right(GreetingConf(WhisperHello,ShoutGoodBye))
```

## Can I configure how the elements are read?

Nope. If you need more flexibility, look at [enumeratum](https://github.com/lloydmeta/enumeratum) and its companion PureConfig library, [pureconfig-enumeratum](https://github.com/leifwickland/pureconfig/tree/master/modules/enumeratum).
