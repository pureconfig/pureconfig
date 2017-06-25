## FAQ

### How can I use PureConfig with Spark 2.1.0 (problematic shapeless dependency)?

Apache Spark (specifically version 2.1.0) has a transitive dependency
on [shapeless](https://github.com/milessabin/shapeless) 2.0.0. This version is
too old to be used by PureConfig, making your Spark project fail when using
`spark-submit`. The solution is to shade, i.e. rename, the version of shapeless
used by PureConfig.

#### SBT

If you are using the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin
to create your JARs you
can [shade](https://github.com/sbt/sbt-assembly#shading) shapeless by
adding

```scala
assemblyShadeRules in assembly := Seq(ShadeRule.rename("shapeless.**" -> "new_shapeless.@1").inAll)
```

to your `assembly.sbt` file.

#### Maven

The [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/)
can shade shapeless by adding

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <createDependencyReducedPom>false</createDependencyReducedPom>
        <relocations>
            <relocation>
                <pattern>shapeless</pattern>
                <shadedPattern>shapelesspureconfig</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
</plugin>
```

to your `pom.xml` file.
