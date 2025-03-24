---
layout: docs
title: Error Handling
---

## {{page.title}}

PureConfig features a rich error model used on reading operations. Most PureConfig methods that read Scala types from
configurations return a `ConfigReader.Result[A]` - an alias for `Either[ConfigReaderFailures, A]`, with `A` being the type of a
successful result and `ConfigReaderFailures` being a non-empty list of errors that caused the reading operation to fail.

From the various types of `ConfigReaderFailure`, one of them is of particular interest: a `ConvertFailure` is an error
occurred during the conversion process itself. It features a reason (`FailureReason`), an optional location in the
config files where the conversion error occurred and a path in the config.

There are several possible `FailureReason`s, the most common being:

- A general, uncategorized reason (`CannotConvert`);
- A required key was not found (`KeyNotFound`);
- A config value has a wrong type (`WrongType`).

For example, given a config like this:

```scala mdoc:silent
import pureconfig._
import pureconfig.generic.auto._

case class Name(firstName: String, lastName: String)
case class Person(name: Name, age: Int)
case class PersonConf(person: Person)
```

Trying to load it with a string instead of an object at `name` results in a `ConvertFailure` because of a `WrongType`:

```scala mdoc
val res = ConfigSource.string("{ person: { name: John Doe, age: 35 } }").load[PersonConf]
```

All error-related classes are present in the `pureconfig.error` package.

### Validations in custom readers

When implementing custom readers, the cursor API already deals with the most common reasons for a reader to fail.
However, it also provides a `failed` method for users to do validations on their side, too:

```scala mdoc:silent
import com.typesafe.config.ConfigValueType._
import scala.util.{Try, Success, Failure}
import pureconfig.error._

case class PositiveInt(value: Int) {
  require(value >= 0)
}

implicit val positiveIntReader: ConfigReader[PositiveInt] = ConfigReader.fromCursor[PositiveInt] { cur =>
  cur.asString.flatMap { str =>
    Try(str.toInt) match {
      case Success(n) if n >= 0 => Right(PositiveInt(n))
      case Success(n) => cur.failed(CannotConvert(str, "PositiveInt", s"$n is not positive"))
      case Failure(_) => cur.failed(WrongType(STRING, Set(NUMBER)))
    }
  }
}

case class Conf(n: PositiveInt)
```

```scala mdoc
ConfigSource.string("{ n: 23 }").load[Conf]
ConfigSource.string("{ n: -23 }").load[Conf]
ConfigSource.string("{ n: abc }").load[Conf]
```

### Custom failure reasons

Users are not restricted to the failure reasons provided by PureConfig. If we wanted to use a domain-specific failure
reason for our `PositiveInt`, for example, we could create it like this:

```scala mdoc:nest:silent
case class NonPositiveInt(value: Int) extends FailureReason {
  def description = s"$value is not positive"
}

implicit val positiveIntReader = ConfigReader.fromCursor[PositiveInt] { cur =>
  cur.asString.flatMap { str =>
    Try(str.toInt) match {
      case Success(n) if n >= 0 => Right(PositiveInt(n))
      case Success(n) => cur.failed(NonPositiveInt(n))
      case Failure(_) => cur.failed(WrongType(STRING, Set(NUMBER)))
    }
  }
}
```

```scala mdoc
ConfigSource.string("{ n: -23 }").load[Conf]
```

### Throwing an exception instead of returning `Either`

In some usage patterns, there isn't a need to deal with errors as values. For example, a good practice to handle configs
in an application is to load the whole config with PureConfig at initialization time, causing the application to fail
fast in case of a malformed config. For those cases, the `loadOrThrow` method can be used instead of `load`:

```scala mdoc
ConfigSource.string("{ n: 23 }").loadOrThrow[Conf]
```

```scala mdoc:crash
ConfigSource.string("{ n: -23 }").loadOrThrow[Conf]
```

The message of the thrown exception contains human-readable information of all the errors found by PureConfig, with the
errors grouped and organized by path.
