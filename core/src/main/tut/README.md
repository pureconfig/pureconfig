# PureConfig

<img src="docs/src/main/resources/microsite/img/pureconfig-logo-1040x1200.png" width="130px" height="150px" align="right">

[![Build Status](https://travis-ci.org/pureconfig/pureconfig.svg?branch=master)](https://travis-ci.org/pureconfig/pureconfig)
[![Coverage Status](https://coveralls.io/repos/github/pureconfig/pureconfig/badge.svg?branch=master)](https://coveralls.io/github/pureconfig/pureconfig?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11)
[![Join the chat at https://gitter.im/melrief/pureconfig](https://badges.gitter.im/melrief/pureconfig.svg)](https://gitter.im/melrief/pureconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

PureConfig is a Scala library for loading configuration files. It reads [Typesafe Config](https://github.com/typesafehub/config) configurations written in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), Java `.properties`, or JSON to native Scala classes in a boilerplate-free way. Sealed traits, case classes, collections, optional values, and many other [types are all supported out-of-the-box](docs/supported-types.html). Users also have many ways to add support for custom types or customize existing ones.

Click on the demo gif below to see how PureConfig effortlessly translates your configuration files to well-typed objects without error-prone boilerplate.
<br clear="right"> <!-- Turn off the wrapping for the logo image. -->

![](http://i.imgur.com/P6sda06.gif)

## Documentation

Read the [documentation](https://pureconfig.github.io/docs).

## Use PureConfig

First, import the library, define data types, and a case class to hold the configuration:

```scala
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

Second, define a configuration. Options for defining this are described in
the [config files documentation](docs/config-files.html):

`application.json`
```json
{ 
  "boolean": true,
  "port": 8080, 
  "adt": { 
    "type": "adtb", 
    "b": 1 
  }, 
  "list": ["1", "20%"], 
  "map": { "key": "value" } 
}
```

Then, load the configuration ([in this case from the classpath](docs/config-files.html)):

```scala
loadConfig[MyClass](conf)
// res3: Either[pureconfig.error.ConfigReaderFailures,MyClass] = Right(MyClass(true,Port(8080),AdtB(1),List(1.0, 0.2),Map(key -> value),None))
```

## Contribute

PureConfig is a free library developed by several people around the world.
Contributions are welcomed and encouraged. If you want to contribute, we suggest to have a look at the
[available issues](https://github.com/melrief/pureconfig/issues) and to talk with
us on the [pureconfig gitter channel](https://gitter.im/melrief/pureconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge).

If you'd like to add support for types which are not part the standard Java or Scala libraries, please consider submitting a pull request to create a [module](docs/integrating.html). [Pull Request #108](https://github.com/melrief/pureconfig/pull/108/files) created a very simple module. It should provide a good template for the pieces you'll need to add.

The steps to create a new module, called _`nexttopmod`_, are:

1. Define a new project in the root `build.sbt`. There are other examples near the top of the file.
2. Create a new  `modules/nexttopmod/` subdirectory.
3. Add a `modules/nexttopmod/build.sbt` defining the module's name and special dependencies.
4. Implement converters. Typically they're in a `package object` in `modules/nexttopmod/src/main/scala/pureconfig/module/nexttopmod/package.scala`.
5. Test the converters. Usually tests would be in `modules/nexttopmod/src/test/scala/pureconfig/module/nexttopmod/NextTopModSuite.scala`.
6. Optionally explain a little bit about how it works in `modules/nexttopmod/README.md`.

PureConfig supports the [Typelevel](http://typelevel.org/) [code of conduct](http://typelevel.org/conduct.html) and wants all of its channels (Gitter, GitHub, etc.) to be
welcoming environments for everyone.


## License

[Mozilla Public License, version 2.0](https://github.com/melrief/pureconfig/blob/master/LICENSE)


## Special Thanks

To the [Shapeless](https://github.com/milessabin/shapeless) and to the [Typesafe Config](https://github.com/typesafehub/config)
developers.

[typesafe-config]: https://github.com/typesafehub/config
