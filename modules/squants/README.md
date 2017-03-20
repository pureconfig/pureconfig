# Squants module for PureConfig

Adds support for [Squants](http://www.squants.com/) library to PureConfig.

## Why

Automatically create a converter to read [Squants](http://www.squants.com/)'s beautiful types representing units of measure from a configuration.

## Add pureconfig-squants to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.melrief" %% "pureconfig-squants" % "0.6.0"
```

## Example

```scala
// Given a type to hold our configuration:
import squants.thermal._
import squants.space._
case class HowConfiguration(far: Length, hot: Temperature)

// We can read a HowConfiguration like:
import pureconfig.loadConfig
import pureconfig.module.squants._
import com.typesafe.config.ConfigFactory.parseString
val conf = parseString("""{
  far: 42.195 km
  hot: 56.7° C
}""")
loadConfig[HowConfiguration](conf)
// Success(HowConfiguration(42.195 km,56.7°C))
```
