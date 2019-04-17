# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig, provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert` and some syntactic sugar for pureconfig
classes.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.10.2"
```

## Example

### Reading cats data structures from a config

The following cats data structures are supported: 

* Cats data types with `Foldable` and `Alternative` (i.e. non-reducible) typeclass instances, e.g. `Chain`.
* `NonEmptyList`, `NonEmptyVector`, `NonEmptySet`, `NonEmptyChain`
* `NonEmptyMap[K, V]` implicits of `ConfigReader[Map[K, V]]` and `Order[K]` should be in the scope.
For example, if your key is a `String` then `Order[String]` can be imported from `cats.instances.string._`

All of these data structures rely on the instances of their unrestricted (i.e. possibly empty) variants.
Custom collection readers, if any, may affect the behavior of these too.

Here is an example of usage:

```scala
import cats.data.{NonEmptyList, NonEmptySet, NonEmptyVector, NonEmptyMap, NonEmptyChain}
import cats.instances.string._
