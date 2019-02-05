# Cats-effect module for PureConfig

Adds support for loading configuration using [cats-effect](https://github.com/typelevel/cats-effect) to control side effects.

## Add pureconfig-cats-effect to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.10.2"
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
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import cats.effect.IO

case class MyConfig(somefield: Int, anotherfield: String)

val load: IO[MyConfig] = loadConfigF[IO, MyConfig](somePath)
```

To test that this `IO` does indeed return a `MyConfig` instance:
```tut:book
//Show the contents of the file
new String(Files.readAllBytes(somePath), StandardCharsets.UTF_8)

load.unsafeRunSync().equals(MyConfig(1234, "some string"))
```

### Writing configuration

To create an IO that writes out a configuration file, do as follows:

```tut:silent
import pureconfig.module.catseffect._
import cats.effect.IO

val someConfig = MyConfig(1234, "some string")
val save: IO[Unit] = saveConfigAsPropertyFileF[IO, MyConfig](someConfig, somePath)
```
