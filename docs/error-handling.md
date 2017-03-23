## Error handling

The `pureconfig.error` package provides semantically rich case classes to
describe failures reading from a `ConfigValue`. When implementing your own
`ConfigConvert`s, you're recommended to use the provided case classes.

The `ConfigReaderFailure` class has an optional location field that can be used
to point to the physical location of a `ConfigValue` that raised an error. When
using the `ConfigReaderFailure` sealed family of case classes, you can use the
`ConfigValueLocation.apply(cv: ConfigValue)` method to automatically create an
optional location from a `ConfigValue`.

Given this setup:

```scala
import com.typesafe.config._
import pureconfig.error._
import java.nio.file.{ Path, Paths }

val cv = ConfigFactory.load.root().get("conf")
```

We can try to load a missing key and get a useful error:
```scala
val notFound = KeyNotFound("xpto", ConfigValueLocation(cv)).location.get
// notFound: pureconfig.error.ConfigValueLocation = ConfigValueLocation(file:/home/derek/pureconfig/docs/target/scala-2.12/classes/application.conf,11)

// print the filename as a relative path
Paths.get(System.getProperty("user.dir")).relativize(Paths.get(notFound.url.toURI))
// res2: java.nio.file.Path = docs/target/scala-2.12/classes/application.conf

notFound.lineNumber
// res3: Int = 11
```
