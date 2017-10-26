---
layout: docs
title: Use pureconfig
---
## {{page.title}}

Import the library package and use one of the `loadConfig` methods:

```tut:silent
import pureconfig._
import pureconfig.error.ConfigReaderFailures

case class YourConfClass(name: String, quantity: Int)

val config: Either[pureconfig.error.ConfigReaderFailures,YourConfClass] = loadConfig[YourConfClass]
```
