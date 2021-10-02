# Joda Time module for PureConfig

Adds support for [Joda Time](http://www.joda.org/joda-time/) to PureConfig.

## Why

Create configurable converters to read [Joda Time](http://www.joda.org/joda-time/) types from configuration.

The converters need to be provided a `org.joda.time.format.DateTimeFormatter` to know how to read values.

## Add pureconfig-joda to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-joda" % "0.17.0"
```

## Example

Define a Joda `DateTimeFormatter` for ISO 8601-encoded date/time strings. The formatter converts datetimes into UTC:

```scala
import org.joda.time.format.ISODateTimeFormat
val isoFormatter = ISODateTimeFormat.dateTimeParser.withZoneUTC
```

Create a ConfigConvert to read DateTime with that format:
```scala
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.joda.configurable._
import com.typesafe.config.ConfigFactory.parseString

implicit val dateTimeConverter = dateTimeConfigConvert(isoFormatter)
```

An object to receive our configuration;
```scala
import org.joda.time.DateTime
case class GreatDatesConfig(apollo: DateTime, pluto: DateTime)
```

We can read a GreatDatesConfig like:

```scala
val conf = parseString("""{
  apollo: "1969-07-20T20:18:00.000Z"
  pluto: "2021-01-20T06:59:59.999Z"
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"apollo":"1969-07-20T20:18:00.000Z","pluto":"2021-01-20T06:59:59.999Z"}))
ConfigSource.fromConfig(conf).load[GreatDatesConfig]
// res0: ConfigReader.Result[GreatDatesConfig] = Right(
//   GreatDatesConfig(1969-07-20T20:18:00.000Z, 2021-01-20T06:59:59.999Z)
// )
```

Note that you'll need to configure a separate converter for each of the Joda Time types that you want to load from your configuration.  For example, call `localDateConfigConvert` to support `LocalDateTime`. Most of the Joda Time types are supported by methods with likewise unsurprising names in the [`joda.configurable` package](src/main/scala/pureconfig/module/joda/configurable/package.scala).
