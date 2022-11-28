# Cats-effect module for PureConfig

Adds support for loading configuration using [cats-effect](https://github.com/typelevel/cats-effect) to control side effects.

## Add pureconfig-cats-effect to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats-effect" % "@VERSION@"
```

## Example

### Reading configuration

To load a configuration file from a path using cats-effect's `IO`:

```scala mdoc:invisible
import java.nio.file.Files
import java.nio.charset.StandardCharsets

val somePath = Files.createTempFile("config", ".properties")
val fileContents = "somefield=1234\nanotherfield=some string"
Files.write(somePath, fileContents.getBytes(StandardCharsets.UTF_8))
```

```scala mdoc:silent
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import cats.effect.IO
import cats.effect.unsafe.implicits._

case class MyConfig(somefield: Int, anotherfield: String)

def load: IO[MyConfig] = {
  ConfigSource.file(somePath).loadF[IO, MyConfig]()
}
```

To test that this `IO` does indeed return a `MyConfig` instance:

```scala mdoc
//Show the contents of the file
new String(Files.readAllBytes(somePath), StandardCharsets.UTF_8)

load.unsafeRunSync().equals(MyConfig(1234, "some string"))
```

### Writing configuration

To create an IO that writes out a configuration file, do as follows:

```scala mdoc:reset:invisible
import java.nio.file.Files

val somePath = Files.createTempFile("config", ".properties")

case class MyConfig(somefield: Int, anotherfield: String)
```

```scala mdoc:silent
import pureconfig.module.catseffect._
import pureconfig.generic.auto._
import cats.effect.IO

val someConfig = MyConfig(1234, "some string")

def save: IO[Unit] = {
  saveConfigAsPropertyFileF[IO, MyConfig](someConfig, somePath)
}
```
