---
layout: home
title: PureConfig
---

# {{page.title}}

<img src="img/pureconfig-logo-1040x1200.png" width="130px" height="150px" align="right" alt="PureConfig">

[![Build Status](https://travis-ci.org/pureconfig/pureconfig.svg?branch=master)](https://travis-ci.org/pureconfig/pureconfig)
[![Coverage Status](https://coveralls.io/repos/github/pureconfig/pureconfig/badge.svg?branch=master)](https://coveralls.io/github/pureconfig/pureconfig?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pureconfig/pureconfig_2.11)
[![Join the chat at https://gitter.im/melrief/pureconfig](https://badges.gitter.im/melrief/pureconfig.svg)](https://gitter.im/melrief/pureconfig?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

PureConfig is a Scala library for loading configuration files. It reads [Typesafe Config](https://github.com/typesafehub/config) configurations written in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), Java `.properties`, or JSON to native Scala classes in a boilerplate-free way. Sealed traits, case classes, collections, optional values, and many other [types are all supported out-of-the-box](docs/built-in-supported-types.html). Users also have many ways to add support for custom types or customize existing ones.

Click on the demo gif below to see how PureConfig effortlessly translates your configuration files to well-typed objects without error-prone boilerplate.
<br clear="right"> <!-- Turn off the wrapping for the logo image. -->

<img src="http://i.imgur.com/P6sda06.gif" style="width: 100%" alt="PureConfig demo">
