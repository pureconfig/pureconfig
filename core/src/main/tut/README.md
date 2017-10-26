# PureConfig

<img src="docs/img/pureconfig-logo-1040x1200.png" width="130px" height="150px" align="right">

[![Build Status](https://travis-ci.org/pureconfig/pureconfig.svg?branch=master)](https://travis-ci.org/pureconfig/pureconfig)
[![Coverage Status](https://coveralls.io/repos/github/pureconfig/pureconfig/badge.svg?branch=master)](https://coveralls.io/github/pureconfig/pureconfig?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11)
[![Join the chat at https://gitter.im/melrief/pureconfig](https://badges.gitter.im/melrief/pureconfig.svg)](https://gitter.im/melrief/pureconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

PureConfig is a Scala library for loading configuration files. It reads [Typesafe Config](https://github.com/typesafehub/config) configurations written in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), Java `.properties`, or JSON to native Scala classes in a boilerplate-free way. Sealed traits, case classes, collections, optional values, and many other [types are all supported out-of-the-box](#supported-types). Users also have many ways to add support for custom types or customize existing ones.

Click on the demo gif below to see how PureConfig effortlessly translates your configuration files to well-typed objects without error-prone boilerplate.
<br clear="right"> <!-- Turn off the wrapping for the logo image. -->

![](http://i.imgur.com/P6sda06.gif)

## Documentation

Read the [documentation](https://pureconfig.github.io/setup-pureconfig.html).

## Example

First, import the library, define data types, and a case class to hold the configuration:

```tut:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.loadConfig

sealed trait MyAdt
case class AdtA(a: String) extends MyAdt
case class AdtB(b: Int) extends MyAdt
final case class Port(value: Int) extends AnyVal
case class MyClass(
  boolean: Boolean,
  port: Port,
  adt: MyAdt,
  list: List[Double],
  map: Map[String, String],
  option: Option[String])
```

Then, load the configuration (in this case from a hard-coded string):

```tut:book
val conf = parseString("""{
  "boolean": true,
  "port": 8080,
  "adt": {
    "type": "adtb",
    "b": 1
  },
  "list": ["1", "20%"],
  "map": { "key": "value" }
}""")

loadConfig[MyClass](conf)
