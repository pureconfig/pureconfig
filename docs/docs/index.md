---
layout: home
---

# PureConfig

<img src="img/pureconfig-logo-1040x1200.png" width="130px" height="150px" align="right" alt="PureConfig">

[![Build Status](https://github.com/pureconfig/pureconfig/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/pureconfig/pureconfig/actions?query=workflow%3ACI+branch%3Amaster)
[![Coverage Status](https://coveralls.io/repos/github/pureconfig/pureconfig/badge.svg?branch=master)](https://coveralls.io/github/pureconfig/pureconfig?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.pureconfig/pureconfig_2.13)](https://central.sonatype.com/artifact/com.github.pureconfig/pureconfig_2.13/overview)
[![Scaladoc](https://javadoc.io/badge/com.github.pureconfig/pureconfig-core_2.13.svg)](https://javadoc.io/page/com.github.pureconfig/pureconfig-core_2.13/latest/pureconfig/index.html)
[![Join the chat at https://gitter.im/melrief/pureconfig](https://badges.gitter.im/melrief/pureconfig.svg)](https://gitter.im/melrief/pureconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

PureConfig is a Scala library for loading configuration files. It reads [Typesafe Config](https://github.com/lightbend/config) configurations written in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), Java `.properties`, or JSON to native Scala classes in a boilerplate-free way. Sealed traits, case classes, collections, optional values, and many other [types are all supported out-of-the-box](docs/built-in-supported-types.html). Users also have many ways to [add support for custom types](docs/supporting-new-types.html) or [customize existing ones](docs/overriding-behavior-for-types.html).

<br clear="right"> <!-- Turn off the wrapping for the logo image. -->
