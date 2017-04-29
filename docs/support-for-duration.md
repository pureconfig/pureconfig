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
 
```scala
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.syntax._
import scala.concurrent.duration._
def convert(s: String) = parseString(s"d = $s").getValue("d").toOrThrow[FiniteDuration]
```
```scala
convert("28 days")
// res0: scala.concurrent.duration.FiniteDuration = 28 days

convert("127 hours")
// res1: scala.concurrent.duration.FiniteDuration = 127 hours

convert("88 min")
// res2: scala.concurrent.duration.FiniteDuration = 88 minutes

convert("7 s")
// res3: scala.concurrent.duration.FiniteDuration = 7 seconds

convert("215 millis")
// res4: scala.concurrent.duration.FiniteDuration = 215 milliseconds

convert("15 µs")
// res5: scala.concurrent.duration.FiniteDuration = 15 microseconds

convert("3 nanoseconds")
// res6: scala.concurrent.duration.FiniteDuration = 3 nanoseconds

convert("0")
// res7: scala.concurrent.duration.FiniteDuration = 0 days
```

### Duration

For [`Duration`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration.html) PureConfig supports the features mentioned above plus the following for the various flavors of [`Infinite`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration$$Infinite.html) duration.

- `Inf`, `PlusInf`, or `+Inf`
- `MinusInf` or `-Inf`
- `Undefined`


For example:

```scala
def convert(s: String) = parseString(s"d = $s").getValue("d").toOrThrow[Duration]
```
```scala
convert("Inf")
// res8: scala.concurrent.duration.Duration = Duration.Inf

convert("-Inf")
// res9: scala.concurrent.duration.Duration = Duration.MinusInf

convert("Undefined")
// res10: scala.concurrent.duration.Duration = Duration.Undefined
```
