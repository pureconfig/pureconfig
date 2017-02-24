## Error handling

The `pureconfig.error` package provides semantically rich case classes to
describe failures reading from a `ConfigValue`. When implementing your own
`ConfigConvert`s, you're recommended to use the provided case classes.

The `ConfigReaderFailure` class has an optional location field that can be used
to point to the physical location of a `ConfigValue` that raised an error. When
using the `ConfigReaderFailure` sealed family of case classes, you can use the
`ConfigValueLocation.apply(cv: ConfigValue)` method to automatically create an
optional location from a `ConfigValue`:

```scala
import com.typesafe.config._
import pureconfig.error._

val cv = ConfigFactory.load.root().get("conf")
// cv: com.typesafe.config.ConfigValue = SimpleConfigObject({"a":1,"b":2})

KeyNotFound("xpto", ConfigValueLocation(cv))
// returns KeyNotFound(xpto,Some(ConfigValueLocation(/work/pureconfig/core/target/scala-2.12/classes/application.conf,0)))
```
