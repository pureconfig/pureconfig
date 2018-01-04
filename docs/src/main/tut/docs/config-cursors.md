---
layout: docs
title: Config Cursors
---

## {{page.title}}

When a `ConfigReader` needs to be created from scratch, users need to implement a `from` method with the following
signature:

```scala
def from(cur: ConfigCursor): Either[ConfigReaderFailures, A]
```

The `ConfigCursor` class is a wrapper for the raw `ConfigValue` provided by Typesafe Config. It provides an idiomatic,
typesafe API for the most common operations needed while reading a config. In particular, it provides cast operations
and key accesses that integrate neatly with the [PureConfig errors API](error-handling.html). When using cursors
properly, most errors are automatically handled and filled with rich information about the location of the failure.

We'll show how to implement our own `ConfigReader` for the following class:

```tut:silent
class Person(firstName: String, lastNames: Array[String]) {
  override def toString = s"Person($firstName ${lastNames.mkString(" ")})"
}

case class Conf(person: Person)
```

We intend our config to look like this:

```tut:silent
import com.typesafe.config.ConfigFactory

val conf = ConfigFactory.parseString("person.name: John Doe")
```

For the purposes of this example, we'll assume the provided `name` will always have at least two words.

An implementation of the `ConfigReader` using the cursors API is shown below:

```tut:silent
import pureconfig._

def firstNameOf(name: String): String =
  name.takeWhile(_ != ' ')

def lastNamesOf(name: String): Array[String] =
  name.dropWhile(_ != ' ').drop(1).split(" ")

implicit val personReader = ConfigReader.fromCursor[Person] { cur =>
  for {
    objCur <- cur.asObjectCursor.right      // 1
    nameCur <- objCur.atKey("name").right   // 2
    name <- nameCur.asString.right          // 3
  } yield new Person(firstNameOf(name), lastNamesOf(name))
}
```

The factory method `ConfigReader.fromCursor` allows us to create a `ConfigReader` without much boilerplate by providing
the required `ConfigCursor => Either[ConfigReaderFailures, A]` function. Since most methods in the cursor API return
`Either` values with failures at their left side,
[for comprehensions](https://docs.scala-lang.org/tour/for-comprehensions.html) are a natural fit (note that on Scala
2.12 and above, the `.right` projections at the end of the `Either` results aren't needed). Let's analyze the lines
marked above:

1. `asObjectCursor` casts a cursor to a special `ConfigObjectCursor`, which contains methods exclusive to config
objects. If the provided config value is not an object, the method returns a `Left` and the execution stops here;
2. `atKey` is defined only on object cursors and accesses a given key on the underlying object. Once more, trying to
access a non-existing key results in an error, stopping the for comprehension;
3. having a cursor for the `name` key we want, `asString` tries to cast the config value pointed to by the cursor to a
string.

Loading a well-formed config will now work correctly:

```tut:book
loadConfig[Conf](conf)
```

While malformed configs will fail to load with appropriate errors:

```tut:book
loadConfig[Conf](ConfigFactory.parseString("person = 45"))
loadConfig[Conf](ConfigFactory.parseString("person.eman = John Doe"))
loadConfig[Conf](ConfigFactory.parseString("person.name = [1, 2]"))
```

By using the appropriate `ConfigCursor` methods, all error handling was taken care of by PureConfig. That makes
PureConfig easy to use even when users have to deal with the low-level details of the conversions.
