---
layout: docs
title: Configurable Converters
---

## {{page.title}}

For some types, PureConfig cannot automatically derive a reader because there are multiple ways to convert a configuration value to them. For instance, for [`LocalDate`](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html) PureConfig cannot derive a reader because there are multiple [`DateTimeFormatter`](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)s that can be used to convert a string into a `LocalDate`. Examples of different formats are `yyyy-mm-dd`, e.g. `"2016-01-01"`, and `yyyymmdd`, e.g. `"20160101"`.

For those types, PureConfig provides a way to create readers from the necessary parameters. These methods can be found under the package `pureconfig.configurable`. Once the output of a `pureconfig.configurable` method for a certain type is in scope, PureConfig can start using that configured reader.

Define a case class to hold your configuration, and create a configurable reader:

```tut:silent
import com.typesafe.config.ConfigFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pureconfig._
import pureconfig.configurable._

case class Conf(date: LocalDate)

implicit val localDateInstance = localDateConfigConvert(DateTimeFormatter.ISO_DATE)
```

Then load the configuration:

```tut:book
loadConfig[Conf](ConfigFactory.parseString("{ date: 2011-12-03 }"))
```
