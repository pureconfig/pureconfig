# Joda Time module for PureConfig

Adds support for [Joda Time](http://www.joda.org/joda-time/) to PureConfig.

## Why

Create configurable converters to read [Joda Time](http://www.joda.org/joda-time/) types from from configuration.

The converters need to be provided a `org.joda.time.format.DateTimeFormatter` to know how to read values.

## Add pureconfig-joda to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.melrief" %% "pureconfig-joda" % "0.5.1"
```

## Example

```scala
// Define a Joda `DateTimeFormatter` for a style of writing dates which looks suspiciously like ISO 8601.
import org.joda.time.format.DateTimeFormat
val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

// Create a ConfigConvert to read DateTime with that format.
import pureconfig.module.joda.configurable._
implicit val dateTimeConverter = dateTimeConfigConvert(isoFormatter)

// An object to receive our configuration
import org.joda.time.DateTime
case class GreatDatesConfig(apollo: DateTime, pluto: DateTime)

// We can read a GreatDatesConfig like:
import pureconfig.loadConfig
import com.typesafe.config.ConfigFactory.parseString
val conf = parseString("""{ 
  apollo: "1969-07-20T20:18:00.000Z"
  pluto: "2021-01-20T06:59:59.999Z"
}""")
loadConfig[GreatDatesConfig](conf)
// Success(GreatDatesConfig(1969-07-20T14:18:00.000-06:00,2021-01-19T23:59:59.999-07:00))
```

Note that you'll need to configure a separate converter for each of the Joda Time types that you want to load from your configuration.  For example, call `localDateConfigConvert` to support `LocalDateTime`. Most of the Joda Time types are supported by methods with likewise unsurprising names in the [`joda.configurable` package](src/main/scala/pureconfig/module/joda/configurable/package.scala).
