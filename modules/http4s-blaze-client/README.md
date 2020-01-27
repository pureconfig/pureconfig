
# Http4s Blaze client module for PureConfig

Adds support for [Http4s](http://http4s.org/)'s Blaze client to PureConfig.
It provides a `ConfigReader` for custom `BlazeClientBuilderConfig`,
which in turn creates `BlazeClientBuilder` via the `configure` method.

## Add pureconfig-http4s-blaze-client to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-http4s-blaze-client" % "0.12.2"
```

## Example

```scala
import java.util.concurrent.TimeUnit

import cats.effect.{ContextShift, IO}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.syntax._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
```

We can read a `BlazeClientBuilderConstructor` and create a client with the following code:

```scala
implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

val conf = ConfigFactory.parseString(s"""{ responseHeaderTimeout: "60 s" }""")
val constructor = conf.to[BlazeClientBuilderConfig]

val client = constructor.right.value.configure[IO](global).resource
```
