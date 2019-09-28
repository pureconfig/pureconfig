
# Cron4s module for PureConfig

Adds support for [Cron4s](https://github.com/alonsodomin/cron4s)'s CronExpr class to PureConfig.

## Add pureconfig-cron4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cron4s" % "0.12.1"
```

## Example

To load an `CronExpr` into a configuration, create a class to hold it:

```scala
import cron4s.expr.CronExpr
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.cron4s._

case class MyConfig(schedule: CronExpr)
```

We can read a `MyConfig` with the following code:

```scala
val conf = parseString("""{ schedule: "10-35 2,4,6 * ? * *" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"schedule":"10-35 2,4,6 * ? * *"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: pureconfig.ConfigReader.Result[MyConfig] = Right(MyConfig(10-35 2,4,6 * ? * *))
```
