---
layout: docs
title: Overriding Behavior for Types
---

## {{page.title}}

It is possible to override the behavior of PureConfig for a given type `A` just by putting another implicit instance
of `ConfigReader[A]` in scope. This happens because the newly defined implicit value will have a higher priority than
the ones defined by PureConfig, according to the Scala [implicit precedence rules](https://stackoverflow.com/questions/5598085/where-does-scala-look-for-implicits/5598107#5598107).

For instance, the default behavior of PureConfig for `String` is to return the string itself in the configuration:

```scala mdoc:silent
import com.typesafe.config.ConfigValueFactory
import pureconfig._
import pureconfig.generic.auto._
```

```scala mdoc
ConfigReader[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```

Now let's say that we want to override this behavior such that `String`s are always read lower case. We can define a
custom `ConfigReader` instance for `String`:

```scala mdoc:silent
import pureconfig.ConvertHelpers._

implicit val overrideStrReader = ConfigReader.fromString[String](catchReadError(_.toLowerCase))
```

PureConfig will now use the custom `overrideStrReader` instance:

```scala mdoc
ConfigReader[String].from(ConfigValueFactory.fromAnyRef("FooBar"))
```

All the techniques described in [Supporting New Types](supporting-new-types.html) can be used to define the higher
priority reader.

PureConfig has more fine-grained ways to configure the default readers for case classes and sealed families.
The [Case Classes](overriding-behavior-for-case-classes.html) and
[Sealed Families](overriding-behavior-for-sealed-families.html) subsections show how to do that.
