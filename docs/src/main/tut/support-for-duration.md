## Support for Duration

PureConfig aims to support a superset of the features that Typesafe Config offers for loading a `scala.concurrent.duration.Duration` or `scala.concurrent.duration.FiniteDuration`.

### FiniteDuration

[`FiniteDuration`](http://scala-lang.org/api/current/scala/concurrent/duration/FiniteDuration.html)s can be specified with a wide variety of units.

- `d`, `day`, or `days`
- `h`, `hour`, or `hours`
- `m`, `min`, `minute`, or `minutes`
- `s`, `sec`, `second`, or `seconds`
- `ms`, `milli`, `millis`, `millisecond`, or `milliseconds`
- `us`, `µs`, `micro`,  `micros`, `microsecond` or `microseconds`
- `ns`, `nano`, `nanos`, `nanosecond`, or `nanoseconds`

All durations must have a unit except for `0` which may be specified as a bare scalar. Scala values may be integral or floating point.

For example:
 
```tut:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.syntax._
import scala.concurrent.duration._
def convert(s: String) = parseString(s"d = $s").getValue("d").toOrThrow[FiniteDuration]
```
```tut:book
convert("28 days")
convert("127 hours")
convert("88 min")
convert("7 s")
convert("215 millis")
convert("15 µs")
convert("3 nanoseconds")
convert("0")
```

### Duration

For [`Duration`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration.html) PureConfig supports the features mentioned above plus the following for the various flavors of [`Infinite`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration$$Infinite.html) duration.

- `Inf`, `PlusInf`, or `+Inf`
- `MinusInf` or `-Inf`
- `Undefined`


For example:

```tut:silent
def convert(s: String) = parseString(s"d = $s").getValue("d").toOrThrow[Duration]
```
```tut:book
convert("Inf")
convert("-Inf")
convert("Undefined")
```
