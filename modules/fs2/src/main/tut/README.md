# fs2 module for PureConfig

Adds support for loading and saving configurations from [fs2](https://github.com/functional-streams-for-scala/fs2) streams.

## Add pureconfig-fs2 to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-fs2" % "0.9.2"
```

## Example
### Reading configuration

To load a configuration file from a path using cats-effect's `IO`:

```tut:invisible
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

val somePath = Files.createTempFile("config", ".properties")
val fileContents = "somefield=1234\nanotherfield=some string"
Files.write(somePath, fileContents.getBytes(StandardCharsets.UTF_8))
```

```tut:silent
import pureconfig.generic.auto._
import pureconfig.module.fs2._
import cats.effect.{IO, ContextShift}
import fs2.io.file

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

import java.util.concurrent.Executors

case class MyConfig(somefield: Int, anotherfield: String)

val chunkSize = 4096

implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
val blockingEc: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

val configStream = file.readAll[IO](somePath, blockingEc, chunkSize)

val load: IO[MyConfig] = streamConfig[IO, MyConfig](configStream, blockingEc)
```

To test that this `IO` does indeed return a `MyConfig` instance:
```tut:book
//Show the contents of the file
new String(Files.readAllBytes(somePath), StandardCharsets.UTF_8)

load.unsafeRunSync().equals(MyConfig(1234, "some string"))
```

### Writing configuration

To create a byte stream from a configuration:

```tut:silent
import pureconfig.module.fs2._
import fs2.text
import cats.effect.IO

import cats.instances.string._

val someConfig = MyConfig(1234, "some string")

val configStream: fs2.Stream[IO, Byte] = saveConfigToStream(someConfig)
```

And to confirm the stream has the values we expect:

```tut:book
configStream.through(text.utf8Decode).to(fs2.Sink.showLinesStdOut).compile.drain.unsafeRunSync
```
