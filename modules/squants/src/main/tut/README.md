# Squants module for PureConfig

Adds support for [Squants](http://www.squants.com/) library to PureConfig.

## Why

Automatically create a converter to read [Squants](http://www.squants.com/)'s beautiful types representing units of measure from a configuration.

## Add pureconfig-squants to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-squants" % "0.11.0"
```

## Example

Given a type to hold our configuration:

```tut:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.squants._
import squants.thermal._
import squants.space._

case class HowConfiguration(far: Length, hot: Temperature)
```

We can read a HowConfiguration like:

```tut:book
val conf = parseString("""{
  far: 42.195 km
  hot: 56.7° C
}""")
loadConfig[HowConfiguration](conf)
```
