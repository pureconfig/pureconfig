# Cats-effect2 module for PureConfig

Adds support for loading configuration using [old cats-effect 2.* series](https://github.com/typelevel/cats-effect) to control side effects.
This is a backport of `pureconfig-cats-effect` to the old 2.* series.

## Add pureconfig-cats-effect2 to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats-effect2" % "0.17.8"
```

## Example

### Reading configuration

To load a configuration file from a path using cats-effect's `IO`:


```scala
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect2.syntax._
import cats.effect.{ Blocker, ContextShift, IO }

case class MyConfig(somefield: Int, anotherfield: String)

def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[MyConfig] = {
  ConfigSource.file(somePath).loadF[IO, MyConfig](blocker)
}
```

To test that this `IO` does indeed return a `MyConfig` instance:


```scala
//Show the contents of the file
new String(Files.readAllBytes(somePath), StandardCharsets.UTF_8)
// res1: String = """somefield=1234
// anotherfield=some string"""

Blocker[IO].use(load).unsafeRunSync().equals(MyConfig(1234, "some string"))
// res2: Boolean = true
```

### Writing configuration

To create an IO that writes out a configuration file, do as follows:


```scala
import pureconfig.module.catseffect2._
import pureconfig.generic.auto._
import cats.effect.{ Blocker, ContextShift, IO }

val someConfig = MyConfig(1234, "some string")

def save(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[Unit] = {
  blockingSaveConfigAsPropertyFileF[IO, MyConfig](someConfig, somePath, blocker)
}
```
