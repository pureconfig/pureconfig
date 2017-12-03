---
layout: docs
title: Error Handling
---

## {{page.title}}

The `pureconfig.error` package provides semantically rich case classes to
describe failures reading from a `ConfigValue`. When implementing your own
`ConfigConvert`s, you're recommended to use the provided case classes.

The `ConfigReaderFailure` class has an optional location field that can be used
to point to the file system location of a `ConfigValue` that raised an error.
When using the `ConfigReaderFailure` sealed family of case classes, you can use
the `ConfigValueLocation.apply(cv: ConfigValue)` method to automatically create
an optional location from a `ConfigValue`.

If we load a config and try to load a missing key:

```tut:silent
import com.typesafe.config._
import pureconfig.error._
import java.nio.file.{ Path, Paths }

val cv = ConfigFactory.load.root().get("conf")
val notFound = ConvertFailure(KeyNotFound("xpto"), ConfigValueLocation(cv), "conf").location.get
```

We can extract useful error data:
```tut:book
// print the filename as a relative path
Paths.get(System.getProperty("user.dir")).relativize(Paths.get(notFound.url.toURI))

notFound.lineNumber
```
