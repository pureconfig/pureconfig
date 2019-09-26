---
layout: docs
title: Loading a Config
---

## {{page.title}}

In [Quick Start](index.html) we used `ConfigSource.default.load[MyClass]` to read a config from an `application.conf`
resource and convert it to a case class. `ConfigSource.default` is an instance of `ConfigSource` - a trait representing
sources from which we can load configuration data. This `ConfigSource` in particular reads and builds a config according
to Typesafe Config's [standard behavior](https://github.com/lightbend/config#standard-behavior), which means it can be
used as a replacement for `ConfigFactory.load` in codebases already using Typesafe Config.

The `ConfigSource` companion object defines many other ready-to-use sources, like:

- `ConfigSource.file` - reads a config from a file in a file system;
- `ConfigSource.resources` - reads a config from resources in your classpath or packaged application;
- `ConfigSource.url` - reads a config from an URL;
- `ConfigSource.string` - reads a literal config from a string.

After you have a config source you can load your config using several methods:

```tut:silent
import pureconfig._
import pureconfig.generic.auto._

case class Conf(name: String, age: Int)

val source = ConfigSource.string("{ name = John, age = 33 }")
```

```tut:book
// reads a config and loads it into a `Either[ConfigReaderFailures, Conf]`
source.load[Conf]

// reads a config and loads it into a `Conf` (throwing if not possible)
source.loadOrThrow[Conf]

// reads a raw config from `source`
source.config()

// reads a config as a `ConfigCursor` (see "Config Cursors" section)
source.cursor()
```

As you use PureConfig you'll find yourself using mostly `load` and `loadOrThrow`. The last two examples would be used
only in more complex or specific scenarios.

### Combining Sources

A common pattern when loading configs is to read and merge from multiple config sources - maybe you have app-specific
and user-specific configs you want to merge in some order, or maybe you want to fall back to some default configuration
if a file doesn't exist or cannot be read.

Most `ConfigSource` instances are also instances of `ConfigObjectSource` - a more specific type of source that is
guaranteed to produce a config object (instead of say, an array or a scalar value). `ConfigObjectSource` instances are
equipped with an `.withFallback` method you can use to merge configs:

```tut:silent
val appSource = ConfigSource.string("{ age = 33 }")
val defaultsSource = ConfigSource.string("{ name = Admin, age = -1 }")
```

```tut:book
appSource.withFallback(defaultsSource).load[Conf]
```

Sometimes you want some of the sources in your chain to be optional. You can call `.optional` on any
`ConfigObjectSource` to make it produce an empty config if the underlying source cannot be read:

```tut:silent
val otherAppSource = ConfigSource.file("non-existing-file.conf")
```

```tut:book
otherAppSource.withFallback(defaultsSource).load[Conf]

otherAppSource.optional.withFallback(defaultsSource).load[Conf]
```

You also have the option to use an alternative source in case your primary source can't be read by using `.recoverWith`:

```tut:book
otherAppSource.recoverWith { case _ => defaultsSource }.load[Conf]
```

### Loading a Config in a Path

You may want your application config to be loaded from a specific path in the config files, e.g. if you want to have
configs for multiple apps in the same sources. `ConfigSource` instances have an `.at` method you can use to specify
where you want the config to be read from:

```tut:silent
val multiAppSource = ConfigSource.string("""
    app-a: {
        timeout: 5s
        retries: 3
    }
    app-b: {
        host: example.com
        port: 8087
    }
""")

case class MyAppConf(host: String, port: Int)
```

```tut:book
multiAppSource.at("app-b").load[MyAppConf]
```

### Customizing Typesafe Config's Behavior

If you want only parts of Typesafe Config's standard behavior or want to customize something in their pipeline,
PureConfig provides `ConfigSource`s like `defaultReference`, `defaultApplication`, `defaultOverrides` and
`systemProperties`, which you can use to mix and match to fit your needs. Take a look at their Scaladoc for more
information about each of them.
