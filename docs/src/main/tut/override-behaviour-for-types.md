---
layout: docs
title: Override behaviour for types
---
## {{page.title}}

It is possible to override the behaviour of PureConfig for a certain type by
implementing another instance of `ConfigReader`, `ConfigWriter` or `ConfigConvert`. For instance,
the default behaviour of PureConfig for `String` is to return the string itself
in the configuration.

When the default type class instances are imported:

```tut:silent
import com.typesafe.config.ConfigValueFactory
import pureconfig._
```

PureConfig returns the string itself, "FooBar" in this example:

```tut:book
ConfigReader[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```

Now let's say that we want to override this behaviour such that `String`s are
always read lower case. We can define a custom `ConfigReader` instance for `String`:

```tut:silent
import com.typesafe.config.ConfigValueFactory
import pureconfig.ConvertHelpers.catchReadError

implicit val overrideStrReader = ConfigReader.fromString[String](catchReadError(_.toLowerCase))
```

PureConfig will now use the custom `overrideStrReader` instance:

```tut:book
ConfigReader[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```

A similar process can be followed to override `ConfigWriter` or `ConfigConvert`.
