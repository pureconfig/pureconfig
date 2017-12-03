---
layout: docs
title: Supported types
---
## {{page.title}}

Currently supported types for fields are:
- `String`, `Boolean`, `Double` (standard
  and percentage format ending with `%`), `Float` (also supporting percentage),
    `Int`, `Long`, `Short`, `URL`, `URI`, `Duration`, `FiniteDuration`;
- [`java.lang.Enum`](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html)
- all collections implementing the `TraversableOnce` trait where the type of
  the elements is in this list;
- `Option` for optional values, i.e. values that can or cannot be in the configuration;
- `Map` with `String` keys and any value type that is in this list;
- everything in [`java.time`](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) (must be
  configured first - see [Configurable converters](/docs/configurable-converters.html));
- [`java.io.File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html);
- [`java.util.UUID`](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html);
- [`java.nio.file.Path`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html);
- [`java.util.regex.Pattern`](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/regex/Pattern.html) and [`scala.util.matching.Regex`](https://www.scala-lang.org/files/archive/api/current/scala/util/matching/Regex.html);
- [`java.math.BigDecimal`](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html), [`java.math.BigInteger`](https://docs.oracle.com/javase/8/docs/api/java/math/BigInteger.html),
  [`scala.math.BigDecimal`](https://www.scala-lang.org/api/2.12.2/index.html#scala.math.BigDecimal), and [`scala.math.BigInt`](https://www.scala-lang.org/api/2.12.2/index.html#scala.math.BigInt);
- Typesafe `ConfigValue`, `ConfigObject` and `ConfigList`;
- value classes for which readers and writers of the inner type are used;
- case classes;
- sealed families of case classes (ADTs);
- `shapeless.HList`s of elements whose type is in this list.
