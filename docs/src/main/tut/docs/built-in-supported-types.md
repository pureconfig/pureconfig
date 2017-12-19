---
layout: docs
title: Built-in Supported Types
---

## {{page.title}}

PureConfig comes with baked-in support for many types, most of them from the standard Java and Scala libraries. When
using those types, users don't have to provide anything else in order to be able to use `loadConfig`.

The currently supported basic types are:

- `String`, `Boolean`, `Double` (standard and percentage format ending with `%`), `Float` (also supporting percentage),
  `Int`, `Long`, `Short`, `URL`, `URI`, `Duration`, `FiniteDuration`;
- [`java.lang.Enum`](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html);
- everything in [`java.time`](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) (must be
  configured first - see [Configurable Converters](configurable-converters.html));
- [`java.io.File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html);
- [`java.util.UUID`](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html);
- [`java.nio.file.Path`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html);
- [`java.util.regex.Pattern`](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/regex/Pattern.html) and
  [`scala.util.matching.Regex`](https://www.scala-lang.org/files/archive/api/current/scala/util/matching/Regex.html);
- [`java.math.BigDecimal`](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html),
  [`java.math.BigInteger`](https://docs.oracle.com/javase/8/docs/api/java/math/BigInteger.html),
  [`scala.math.BigDecimal`](https://www.scala-lang.org/api/2.12.2/index.html#scala.math.BigDecimal), and
  [`scala.math.BigInt`](https://www.scala-lang.org/api/2.12.2/index.html#scala.math.BigInt);
- Typesafe `ConfigValue`, `ConfigObject` and `ConfigList`;
- value classes (for which readers and writers of the inner type are directly used).

Additionally, PureConfig also handles the following collections and composite Scala structures:

- `Option` for optional values, i.e. values that can or cannot be in the configuration, of types on this list;
- collections implementing the `TraversableOnce` trait, where the type of the elements is on this list;
- `Map`s from `String` keys to any value type that is on this list;
- `shapeless.HList`s of elements whose type is on this list;
- case classes;
- sealed families of case classes (ADTs).

The support for these types already covers most simple cases, such as the one shown in [Quick Start](index.html). See
[Supporting New Types](supporting-new-types.html) to see how to support types that are not on those lists and
[Overriding Behavior for Types](overriding-behavior-for-types.html) to change how PureConfig reads the built-in types
above.
