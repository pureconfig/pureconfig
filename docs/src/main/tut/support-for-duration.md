## Support for Duration

PureConfig aims to support a superset of the features that Typesafe Config offers for loading a `scala.concurrent.duration.Duration` or `scala.concurrent.duration.FiniteDuration`.

### FiniteDuration

[`FiniteDuration`](http://scala-lang.org/api/current/scala/concurrent/duration/FiniteDuration.html)s can specified with a wide variety of units.

- `d`, `day`, or `days`
- `h`, `hour`, or `hours`
- `m`, `min`, `minute`, or `minutes`
- `s`, `sec`, `second`, or `seconds`
- `ms`, `milli`, `millis`, `millisecond`, or `milliseconds`
- `us`, `µs`, `micro`,  `micros`, `microsecond` or `microseconds`
- `ns`, `nano`, `nanos`, `nanosecond`, or `nanoseconds`

All durations must have a unit except for `0` which may be specified as a bare scalar.

### Duration

For [`Duration`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration.html) PureConfig supports the features mentioned above plus the following for the various flavors of [`Infinite`](http://scala-lang.org/api/current/scala/concurrent/duration/Duration$$Infinite.html) duration.

- `Inf`, `PlusInf`, or "+Inf"`
- `MinusInf`  or "-Inf"`
- `Undefined`
