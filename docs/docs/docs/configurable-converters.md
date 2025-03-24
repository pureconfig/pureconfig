---
layout: docs
title: Configurable Converters
---

## {{page.title}}

For some types, PureConfig cannot automatically derive a reader because there are multiple ways to convert a
configuration value to them. For instance, for [`LocalDate`](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html) PureConfig cannot derive a reader because there are multiple [`DateTimeFormatter`](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)s that can be used to convert a string into a `LocalDate`. Examples of different formats are `yyyy-mm-dd`, e.g. `"2016-01-01"`, and `yyyymmdd`, e.g. `"20160101"`. Another example is
reading maps with non-string keys: unless a way to convert the keys to and from strings is provided, PureConfig won't
be able to derive a reader.

For those types, PureConfig provides a way to create readers from the necessary parameters. These methods can be found under the package `pureconfig.configurable`. Once the output of a `pureconfig.configurable` method for a certain type is in scope, PureConfig can start using that configured reader.

Define a case class to hold your configuration, and create a configurable reader:

```scala mdoc:silent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pureconfig._
import pureconfig.configurable._
import pureconfig.ConvertHelpers._
import pureconfig.generic.auto._

case class Conf(date: LocalDate, intMap: Map[Int, Int])

implicit val localDateConvert: ConfigConvert[LocalDate] = localDateConfigConvert(DateTimeFormatter.ISO_DATE)
implicit val intMapReader: ConfigReader[Map[Int, Int]] = genericMapReader[Int, Int](catchReadError(_.toInt))
```

Then load the configuration:

```scala mdoc
ConfigSource.string("{ date: 2011-12-03, int-map: { 2: 4, 4: 16 } }").load[Conf]
```
