---
layout: docs
title: Non-Automatic Derivation
---

## {{page.title}}

All the examples in the documentation rely on automatic derivation of readers and writers for case classes and sealed
traits. That is the recommended way of using PureConfig, in line with its core goal of providing a boilerplate-free but
still type-safe way of handling config files.

However, some users may prefer to declare explicitly their intention to derive readers for a type, some advanced users
may have their own ways of deriving readers and some may not want to use derivation at all. PureConfig actually provides
three ways of setting up reader derivation, which are presented in the next sections.

### Automatic

First, let's define an example case class and a config for us to load:

```scala mdoc:silent
import com.typesafe.config.ConfigFactory
import pureconfig._

case class Person(name: String, surname: String)

val conf = ConfigFactory.parseString("{ name: John, surname: Doe }")
```

Automatic reader derivation is used throughout all the documentation pages. It is activated simply by importing
`pureconfig.generic.auto._` everywhere readers are needed (for example, where `ConfigSource#load` is used):

```scala mdoc:silent
import pureconfig.generic.auto._
```

This import provides `ConfigReader` instances for all supported classes out-of-the-box:

```scala mdoc
ConfigSource.fromConfig(conf).load[Person]
```

Internally, derivation is made through the use of [shapeless](https://github.com/milessabin/shapeless), a generic
programming Scala library.

Custom readers can still be placed in the companion object of the respective classes; PureConfig will make sure that
they are used instead of the automatically-derived ones.

### Semi-Automatic

With semi-automatic derivation, readers can still be derived using all the machinery presented on these documentation
pages, but the reader instances are not provided as implicits. Instead, PureConfig provides two one-liner methods to
create derived instances, which you must put somewhere on the implicit scope.

Semi-automatic derivation is enabled by importing `pureconfig.generic.semiauto._`. We can now explicitly define the
reader for `Person` by calling `deriveReader`:

```scala mdoc:invisible:reset
import com.typesafe.config.ConfigFactory
import pureconfig._

case class Person(name: String, surname: String)

val conf = ConfigFactory.parseString("{ name: John, surname: Doe }")
```

```scala mdoc:silent
import pureconfig.generic.semiauto._

implicit val personReader = deriveReader[Person]
```

We are now ready to read `Person` configs:

```scala mdoc
ConfigSource.fromConfig(conf).load[Person]
```

#### Semi-Automatic for Sealed Families

To support a sealed family with semi-automatic derivation, you'll need to provide a derivation for every concrete member
of the family and the base of the family.

```scala mdoc:silent
sealed trait Occupation extends Product with Serializable

object Occupation {
  case class Employed(job: String) extends Occupation
  object Employed {
    implicit val employedReader = deriveReader[Employed]
  }
  case object Unemployed extends Occupation {
    implicit val unemployedReader = deriveReader[Unemployed.type]
  }
  case object Student extends Occupation {
    implicit val studentReader = deriveReader[Student.type]
  }
  implicit val occupationReader = deriveReader[Occupation]
}

case class WorkingPerson(name: String, surname: String, occupation: Occupation)

object WorkingPerson {
  implicit val workingPersonReader = deriveReader[WorkingPerson]
}
```

```scala mdoc
ConfigSource.string("{ name: Isaac, surname: Newton, occupation.type: student }").load[WorkingPerson]
ConfigSource.string("""{ name: David, surname: Shingy, occupation: { type: employed, job: Digital Prophet } }""").load[WorkingPerson]
```

### Manual

When case class and sealed trait derivation is not needed or wanted, we can simply not import anything and define our
reader using any of ways explained in [Supporting New Types](supporting-new-types.html). The `forProductN` helper
methods are convenient for creating readers and writers for case class-like types without generic derivation:

```scala mdoc:invisible:reset
import com.typesafe.config.ConfigFactory

case class Person(name: String, surname: String)

val conf = ConfigFactory.parseString("{ name: John, surname: Doe }")
```

```scala mdoc:silent
import pureconfig._

implicit val personReader = ConfigReader.forProduct2("name", "surname")(Person(_, _))
```

```scala mdoc
ConfigSource.fromConfig(conf).load[Person]
```

If you don't need reader or writer derivation anywhere in your project, you can replace the `pureconfig` Maven
dependency with `pureconfig-core`. `pureconfig-core` contains only the core classes needed by PureConfig, as well as
readers and writers for primitive and collection types. It has the advantage of not depending on shapeless, which can be
useful to prevent version conflicts.
