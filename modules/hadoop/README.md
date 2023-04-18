# Hadoop module for PureConfig

Adds support for selected [Hadoop](http://hadoop.apache.org/) classes to PureConfig.

## Add pureconfig-hadoop to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-hadoop" % "0.17.3"
```

Also, `pureconfig-hadoop` depends on `hadoop-common` with `provided` scope. This means that you should explicitly add a dependency on `hadoop-common` or any other Hadoop library which depends on `hadoop-common`. Usually it would be something like this:

```scala
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.0.0"
```

To use the Hadoop module you need to import:
```scala
import pureconfig.module.hadoop._
```

## Supported classes

* `org.apache.hadoop.fs.Path`

## Example

To load a `Path` into a configuration, we create a class to hold our configuration:

```scala
import org.apache.hadoop.fs.Path
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.hadoop._

case class MyConfig(path: Path)
```

Now we can read a `MyConfig` like:
```scala
val conf = parseString("""{
  path: "hdfs://some.domain/foo/bar.gz"
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"path":"hdfs://some.domain/foo/bar.gz"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(
//   MyConfig(hdfs://some.domain/foo/bar.gz)
// )
```
