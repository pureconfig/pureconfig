---
layout: docs
title: Overriding Behaviour for Types
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

A similar process can be followed to override `ConfigWriter` or `ConfigConvert`. All the techniques described in
[Supporting New Types](supporting-new-types.html) can be used to override existing readers and writers.

PureConfig has more fine-grained ways to configure the default readers and writers for case classes and sealed families.
The [Case Classes](overriding-behaviour-for-case-classes.html) and
[Sealed Families](overriding-behaviour-for-sealed-families.html) subsections show how to do that.
