---
layout: docs
title: Example
---
## {{page.title}}

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
dirwatch.email.recipients=["recipient1@domain.tld","recipient2@domain.tld"]
dirwatch.email.sender="sender@domain.realm"
```

In this example, we only want to load valid email addresses into our configuration. First, we create a custom class to validate and store email addresses:

```tut:silent
import scala.util.{Failure, Success, Try}
import pureconfig.loadConfigOrThrow

object Example {
  
  /**
    * This is not a production-quality email address validator.
    * It is provided only for illustration purposes.
    */
  object Email {
    private val regex = """^([a-zA-Z0-9!#$%&'.*+/=?^_`{|}~;-]+)@([a-zA-Z0-9.-]+)$""".r

    def fromString(str: String): Try[Email] = str match {
      case regex(local, domain) => Success(new Email(s"$local@$domain"))
      case _ => Failure(new IllegalArgumentException(s"$str is not a valid email address"))
    }
  }

  case class Email private (address: String) {
    override def toString: String = address
  }
}
import Example._
```

We can now use the `Email` class in our configuration. To load it, we define some classes that have proper fields and names:

```tut:silent
import java.nio.file.Path

case class EmailConfig(host: String, port: Int, message: String, recipients: Set[Email], sender: Email)
case class DirWatchConfig(path: Path, filter: String, email: EmailConfig)
case class Config(dirwatch: DirWatchConfig)
```

The use of `Email` gives us a chance to use a custom converter:

```tut:silent
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.viaStringTry

import scala.util.Try

implicit val emailConvert: ConfigConvert[Email] = viaStringTry[Email](Email.fromString, _.toString)
```

And then we load the configuration:

```tut:book
val config = loadConfigOrThrow[Config]
```

And that's it.

You can then use the configuration as you want:

```tut:book
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

```tut:book
import com.typesafe.config.ConfigFactory
import pureconfig.syntax._

val config = ConfigFactory.load.to[Config]
println("The loaded configuration is: " + config.toString)
```
