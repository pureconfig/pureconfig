# YAML module for PureConfig

Adds support to PureConfig for reading YAML files as configurations. The module uses [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) to parse YAML documents and reuses the existing PureConfig structure
of `ConfigReader`s and hints to read configurations to domain objects without boilerplate.

## Add pureconfig-yaml to your project

In addition to the [PureConfig core](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-yaml" % "0.12.3"
```

## Example

Given a type to hold our configuration:

```tut:silent
import pureconfig.generic.auto._
import pureconfig.module.yaml._

case class Person(name: String, age: Int, children: List[Person])
```

And a YAML document with the appropriate format:

```tut:silent
import java.nio.file.Files

val yamlFile = Files.createTempFile("conf-", ".yaml")
Files.write(yamlFile, """
  | name: John
  | age: 42
  | children:
  |   - name: Sarah
  |     age: 7
  |     children: []
  |   - name: Andy
  |     age: 10
  |     children: []
  | """.stripMargin.getBytes)
```

We can load the configuration to a `MyConf` instance using a `YamlConfigSource`:

```tut:book
YamlConfigSource.file(yamlFile).load[Person]
```

We can also load a particular namespace inside the YAML file:

```tut:book
YamlConfigSource.file(yamlFile).at("age").load[Int]
```
