# fs2 module for PureConfig

Adds support for loading and saving configurations from [fs2](https://github.com/functional-streams-for-scala/fs2) streams.

## Add pureconfig-fs2 to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-fs2" % "0.17.2"
```

## Example
### Reading configuration

To load a configuration file from a path using cats-effect's `IO`:


```scala
import pureconfig.generic.auto._
import pureconfig.module.fs2._
import cats.effect.IO
import cats.effect.unsafe.implicits._
import fs2.io.file

case class MyConfig(somefield: Int, anotherfield: String)

val chunkSize = 4096

val configStream = fs2.io.file.Files[IO].readAll(somePath, chunkSize)

val load: IO[MyConfig] = streamConfig[IO, MyConfig](configStream)
```

To test that this `IO` does indeed return a `MyConfig` instance:
```scala
//Show the contents of the file
new String(Files.readAllBytes(somePath), StandardCharsets.UTF_8)
// res1: String = """somefield=1234
// anotherfield=some string"""

load.unsafeRunSync().equals(MyConfig(1234, "some string"))
// res2: Boolean = true
```

### Writing configuration

To create a byte stream from a configuration:

```scala
import pureconfig.module.fs2._
import fs2.text
import cats.effect.IO
import cats.effect.unsafe.implicits._

import cats.instances.string._

val someConfig = MyConfig(1234, "some string")

val configStream2: fs2.Stream[IO, Byte] = saveConfigToStream(someConfig)
```

And to confirm the stream has the values we expect:

```scala
configStream.through(text.utf8Decode).printlns.compile.drain.unsafeRunSync()
```
