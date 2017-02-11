## Example

In the [example directory](https://github.com/melrief/pureconfig/tree/master/example/src/main/scala/pureconfig/example)
there is an example of usage of PureConfig. In the example, the idea is to load a configuration for a directory
watcher service. The configuration file
(a real one is available [here](https://github.com/melrief/pureconfig/blob/master/example/src/main/resources/application.conf))
for this program will look like:

```
dirwatch.path="/path/to/observe"
dirwatch.filter="*"
dirwatch.email.host=host_of_email_service
dirwatch.email.port=port_of_email_service
dirwatch.email.message="Dirwatch new path found report"
dirwatch.email.recipients=["recipient1,recipient2"]
dirwatch.email.sender="sender@domain.realm"
```

To load it, we define some classes that have proper fields and names:

```scala
import java.nio.file.Path
import javax.security.auth.kerberos.KerberosPrincipal

case class EmailConfig(host: String, port: Int, message: String, recipients: Set[String], sender: KerberosPrincipal)
case class DirWatchConfig(path: Path, filter: String, email: EmailConfig)
case class Config(dirwatch: DirWatchConfig)
```

The use of `KerberosPrincipal` gives us a chance to use a custom converter:

```scala
import pureconfig._

import javax.security.auth.kerberos.KerberosPrincipal
import scala.util.Try

implicit val k5PrincipalConvert = ConfigConvert.fromString[KerberosPrincipal](s => Try(new KerberosPrincipal(s)))
```

And then we load the configuration:

```scala
val config = loadConfig[Config].get // loadConfig returns a Try
```

And that's it.

You can then use the configuration as you want:

```scala
println("dirwatch.path: " + config.dirwatch.path)
println("dirwatch.filter: " + config.dirwatch.filter)
println("dirwatch.email.host: " + config.dirwatch.email.host)
println("dirwatch.email.port: " + config.dirwatch.email.port)
println("dirwatch.email.message: " + config.dirwatch.email.message)
println("dirwatch.email.recipients: " + config.dirwatch.email.recipients)
println("dirwatch.email.sender: " + config.dirwatch.email.sender)
```

It's also possible to operate directly on `Config` and `ConfigValue` types
of [Typesafe Config][typesafe-config] with the implicit helpers provided in the
`pureconfig.syntax` package:

```scala
import com.typesafe.config.ConfigFactory
import pureconfig.syntax._

val config = ConfigFactory.load.to[Config].get
println("The loaded configuration is: " + config.toString)
```
