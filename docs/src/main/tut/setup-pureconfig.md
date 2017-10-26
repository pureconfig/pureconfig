---
layout: docs
title: Add PureConfig to your project
---
## {{page.title}}

In the sbt configuration file use Scala `2.10`, `2.11` or `2.12`:

```scala
scalaVersion := "2.12.3" // or "2.11.11", "2.10.6"
```

Add PureConfig to your library dependencies. For Scala `2.11` and `2.12`:

```scala
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.8.0"
)
```

For Scala `2.10` you need also the Macro Paradise plugin:

```scala
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.8.0",
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.patch)
)
```

For a full example of `build.sbt` you can have a look at this [build.sbt](https://github.com/melrief/pureconfig/blob/master/example/build.sbt)
used for the example.

Earlier versions of Scala had bugs which can cause subtle compile-time problems in PureConfig.
As a result we recommend only using 2.11.11 or 2.12.1 or newer within the minor series.

