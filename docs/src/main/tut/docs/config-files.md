---
layout: docs
title: Config files
---
## {{page.title}}

By default, PureConfig `loadConfig` methods load all resources in the classpath named:

- `application.conf`,
- `application.json`,
- `application.properties`, and
- `reference.conf`.

The various `loadConfig` methods defer to Typesafe Config's
[`ConfigFactory`](https://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigFactory.html) to
select where to load the config files from. Typesafe Config has [well-documented rules for configuration
loading](https://github.com/typesafehub/config#standard-behavior) which we'll not repeat. Please see Typesafe
Config's documentation for a full telling of the subtleties. If you need greater control over how config
files are loaded, refer to `ConfigFactory` options.

Alternatively, PureConfig also provides a `loadConfigFromFiles` method, which builds a configuration from
an explicit list of files. Files earlier in the list have greater precedence than later ones. Each file can
include a partial configuration as long as the whole list produces a complete configuration. For an example,
see the test of `loadConfigFromFiles` in
[`ApiSuite.scala`](https://github.com/pureconfig/pureconfig/blob/master/core/src/test/scala/pureconfig/ApiSuite.scala).

Because PureConfig uses Typesafe Config to load configuration, it supports reading files in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), JSON, and Java `.properties` formats. HOCON is a delightful superset of both JSON and `.properties` that is highly recommended. As an added bonus it supports [advanced features](https://github.com/typesafehub/config/blob/master/README.md#features-of-hocon) like variable substitution and file sourcing.
