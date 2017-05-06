## Playing nice with Spark's Shapeless

Apache Spark (specifically version 2.1.0) has a transitive dependency
on [Shapeless](https://github.com/milessabin/shapeless) 2.0.0. This version is
too old to be used by PureConfig, making your Spark project fail when using
`spark-submit`.

If you are using the [sbt-assembly](https://github.com/sbt/sbt-assembly) plugin
to create your JARs you
can [shade this dependency](https://github.com/sbt/sbt-assembly#shading) by
adding

```scala
assemblyShadeRules in assembly := Seq(ShadeRule.rename("shapeless.**" -> "new_shapeless.@1").inAll)
```

to your `assembly.sbt` file.
