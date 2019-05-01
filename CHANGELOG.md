### 0.10.3 (unreleased)

- Breaking changes
  - The default transformation in `FieldCoproductHint` changed from converting class names to lower case to converting
    them to kebab case (e.g. `ConfigOption` is converted to `config-option`). The old behavior can be retained by
    putting in scope an `implicit def coproductHint[T] = new FieldCoproductHint[T]("type") { override def
    fieldValue(name: String): String = name.toLowerCase }`;

- New features
  - Added `deriveEnumerationReader`, `deriveEnumerationWriter` and `deriveEnumerationConvert` to the
    `pureconfig.generic.semiauto` package, allowing the derivation of readers and writers for enumerations encoded as
    sealed traits of case objects. As a consequence, the `EnumCoproductHint` is now deprecated in favor of these new
    methods.

### 0.10.2 (Feb 5, 2019)

- New features
  - Added `ConfigReader.Result[A]` as an alias for `Either[ConfigReaderFailures, A]`;
  - Introduced `FluentConfigCursor`, an alternative API to `ConfigCursor` focused on config navigation over error handling.

### 0.10.1 (Nov 30, 2018)

- New features
  - `loadConfigFromFiles` now accepts a `namespace` parameter like the other `loadConfig*` varieties. (#437)

- Bug fixes
  - `scala-compiler` and `scala-reflect` dependencies are now `provided`, rather than regular, dependencies. (#434)

### 0.10.0 (Oct 30, 2018)

- Breaking changes
  - Auto derivation of readers and writers for case classes and sealed traits is now disabled by default. Now users need
    to import `pureconfig.generic.auto._` everywhere a config is loaded or written (e.g. in files with calls to
    `loadConfig`);
  - The `AllowMissingKeys` trait was renamed to `ReadsMissingKeys`;
  - Dropped support for Scala 2.10.

- New features
  - The auto-derivation features of PureConfig, powered by shapeless, were extracted to a separate `pureconfig-generic`
    module, while `pureconfig-core` was left with only the absolute minimum for PureConfig to be useful. `pureconfig`
    will continue to be published as a Maven artifact aggregating the two aforementioned artifacts;
  - Users have now more control over reader and writer derivation. See the
    [docs](https://pureconfig.github.io/docs) for more information;
  - New factory methods `forProduct1`, `forProduct2`, ..., `forProduct22` were added to the companion objects of
    `ConfigReader` and `ConfigWriter`;
  - A new `WritesMissingKeys` trait enables custom writers to handle missing keys, a feature previously restricted to
    the built-in `Option` writer;
  - Cursors now perform the
    [automatic type conversions](https://github.com/lightbend/config/blob/master/HOCON.md#automatic-type-conversions)
    required by HOCON when `as<type>` methods are called. Cursors now provide `asBoolean`, `asLong`, `asInt`, `asShort`,
    `asDouble` and `asFloat`.

### 0.9.2 (Aug 23, 2018)

- New features
  - Users can now configure whether `loadConfigFromFiles` ignores or fails on non-existing or unreadable files;
  - Custom `ConfigRenderOptions` can now be passed to all config writing API methods;
  - PureConfig can now read `Period` instances written in the human-readable format supported by
    [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md#period-format);
  - New configurable instances allow reading and writing maps with any key type, provided the respective
    conversion to/from strings;
  - `ConfigCursor` has a new `atPath` method for deep navigation into the config;
  - New `pureconfig-yaml` module adding support to load configs from YAML files.

- Bug fixes
  - Fixed a bug where PureConfig was not working when custom preludes were used.

### 0.9.1 (Mar 22, 2018)

- New features
  - `ConfigReader` and `ConfigWriter` for `Char`;
  - Modules for `fs2`, `hadoop` and `http4s`.

### 0.9.0 (Jan 8, 2018)

- New features
  - A new `ConfigCursor` now provides idiomatic, safe methods to navigate through a config. It also holds context for
    building failures with a more accurate location and path in the config;
  - `ConfigReaderFailure` was revamped to facilitate the propagation of context on failures. There is now a separation
    between higher-level `ConfigReaderFailures` and concrete, location-agnostic `FailureReason`s.

- Breaking changes
  - `ConfigReader`, as well as many related methods and classes, now reads configs from `ConfigCursor` instances instead
    of from direct `ConfigValue`s. Code can be migrated simply by accessing the `value` field of `ConfigCursor` whenever
    a `ConfigValue` is needed. However, rewriting the code to use the new `ConfigCursor` methods is heavily recommended
    as it provides safer config handling and much better error handling;
  - Code for handling and raising failures may not work due to the revamp of the failure model. Inside `ConfigReader`
    instances users should now use the `failed` method of the new `ConfigCursor` instead of manually creating instances
    of `ConfigReaderFailures`;
  - The `CannotConvertNull` failure was removed, being superseeded by `KeyNotFound`;
  - Methods deprecated in previous versions were removed.
  
- Bug fixes
  - Fixed a bug where some or all `Derivation` cases outside the `pureconfig` package were not showing the full error
    description.

### 0.8.0 (Aug 27, 2017)

- New features
  - `loadConfig` methods now allow loading any type from a config when using a namespace, and not only types represented
    by config objects;
  - `ConfigFieldMapping` now has a `withOverrides` method that allows users to easily define exceptional cases to an
    existing mapping;
  - `ConfigReader` and `ConfigWriter` for `java.math.BigDecimal` and `java.math.BigInteger`;
  - `ConfigReader` for `Boolean`s allows reading them from "yes", "no", "on" and "off" strings;
  - `ConfigReader` and `ConfigWriter` for `shapeless.HList`;
  - `ConfigReader` for Scala tuples can now read from `ConfigLists`s;
  - Added an experimental way to debug when a converter fails to be derived because an implicit is not found. See
    [the documentation](https://pureconfig.github.io/docs/debugging-implicits-not-found.html) for
    more information on how to enable it.
  
- Breaking changes
  - `ConfigWriter` for tuples now writes them as `ConfigList`s, instead of a `ConfigObject` with keys `_1`, `_2`, and so on.
  
- Bug fixes
  - A breaking change introduced in v0.7.1 where `loadConfigFromFiles` stopped allowing missing files was reverted;
  - `loadConfig` methods no longer throw an exception when passed a namespace where one of the keys is not a config
    object;
  - The `xmap` of `ConfigConvert` and the `map` method of `ConfigReader` now wrap exceptions that the functions used to
    map might throw in a `ConfigReaderFailure`;
  - `FieldCoproductHint` now removes the disambiguating key from the config object before passing it to the reader of a
    coproduct option.

### 0.7.2 (May 29, 2017)

- Bug fixes
  - Fix value class `ConfigReader` and `ConfigWriter` derivation [[#253](https://github.com/pureconfig/pureconfig/pull/253)].

### 0.7.1 (May 28, 2017)

- New features
  - `ConfigReader`, `ConfigWriter` and `ConfigConvert` now have combinators such as `map`, `flatMap` and `contramap`,
    making them easier to compose;
  - New mechanism to read and write [value classes](http://docs.scala-lang.org/overviews/core/value-classes.html).
    The readers and writers of the inner type are used instead of the ones for products;
  - New `EnumCoproductHint` for sealed families of case objects where objects are written and read as strings with their
    type names;
  - `ConfigReader` and `ConfigWriter` instances for `Pattern` and `Regex`;
  - `ConfigReader` and `ConfigWriter` instances for `java.time.Duration`;
  - `ConfigReader` and `ConfigWriter` for `java.io.File`;
  - `ConfigReader` and `ConfigWriter` for arbitrary Java `enum`s;
  - Improved error messages when a failure occurs reading a config;
- Bug fixes
  - `Duration.Undefined` is correctly handled when reading and writing configurations [[#184](https://github.com/pureconfig/pureconfig/issues/184)];
  - `loadConfig` method now handles properly cases where a requested config file cannot be read and when a provided
    namespace doesn't exist.

### 0.7.0 (Apr 2, 2017)

- New features
  - `ConfigConvert` is now a union of two new traits - `ConfigReader` for reading configs and `ConfigWriter` for writing
    them:
    - Having an implicit `ConfigReader` in scope is enough to read a config to a instance of a given type;
    - Having a `ConfigWriter` is enough for writing instances to configs;
    - `ConfigConvert` can still be used everywhere it was before and is advisable when both operations are needed.
  - Many constructors for `ConfigConvert` instances were deprecated, while new ones were added in the companion objects
    of `ConfigReader`, `ConfigWriter` and `ConfigConvert`. The deprecation message of each one indicates the new method
    to use;
  - Add `ConfigFactoryWrapper` to control exceptions from typesafe `ConfigFactory`;
  - Modify the message of `ConfigReaderException` to group errors by keys in the configuration, instead of by type of
    error;
  - Add a path (`Option[String]`) to `ConfigReaderFailure`, in order to expose more information (if available) about the
    key in the configuration whose value raised the failure.

- Breaking changes
  - `loadConfigFromFiles` works on `Path` instead of `File` for consistency;
  - `ConfigValueLocation` now uses `URL` instead of `Path` to encode locations of `ConfigValue`s.

- Bug fixes
  - `pureconfig.load*` methods don't throw exceptions on malformed configuration anymore
     and wrap errors in `ConfigReaderFailures` [[#148](https://github.com/pureconfig/pureconfig/issues/148)].

### 0.6.0 (Feb 14, 2017)

- New features
  - New `ProductHint` trait allowing customization of the derived `ConfigConvert` for case classes, superseeding
    `ConfigFieldMapping` ([docs](https://pureconfig.github.io/docs/override-behavior-for-case-classes.html)). In
    addition to defining field name mappings, `ProductHint` instances control:
    - Whether default field values should be used when
      fields are missing in the config ([docs](https://pureconfig.github.io/docs/override-behavior-for-case-classes.html#default-field-values));
    - Whether unknown keys are ignored or cause pureconfig to return a `Failure`
      ([docs](https://pureconfig.github.io/docs/override-behavior-for-case-classes.html#unknown-keys)).
  - Support for reading and writing [`java.util.UUID`](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html)s;
  - Support for reading and writing [`java.nio.file.Path`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html)s;
  - Support for reading and writing [`java.net.URI`](https://docs.oracle.com/javase/8/docs/api/java/net/URI.html)s;
  - Support multiple failures, e.g. when multiple fields of a class fail to convert;
  - Add `ConfigReaderFailure` ADT to model failures and `ConfigReaderFailures` to represent a non empty list of errors;
  - Add `ConfigValueLocation`, which is the physical location of a ConfigValue represented by a file name and a line number;
  - Add `loadConfigOrThrow` methods to the API;
  - Add helpers to create `ConfigConvert`:
    - `ConfigConvert.fromStringConvert` that requires a function `String => Either[ConfigReaderFailure, T]`
    - `ConfigConvert.fromStringConvertTry` that requires a function `String => Try[T]`
    - `ConfigConvert.fromStringConvertOpt` that requires a function `String => Option[T]`
  - Add `ConfigConvert.catchReadError` to convert a function that can throw exception into a safe function that returns
    a `Either[CannotConvert, T]`;

- Breaking changes
  - `ConfigConvert.from` now returns a value of type `Either[ConfigReaderFailures, T]` instead of `Try[T]`;
  - `CoproductHint` has been changed to adapt to the new `ConfigConvert`:
    - `CoproductHint.from` now returns a value of type `Either[ConfigReaderFailures, Option[ConfigValue]]`
    - `CoproductHint.to` now returns a value of type `Either[ConfigReaderFailures, Option[ConfigValue]]`
  - The default field mapping changed from camel case config keys (e.g. `exampleKey`) to kebab case keys (e.g.
    `example-key`). Case class fields are still expected to be camel case. The old behavior can be retained by putting
    in scope an `implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))`;
  - `ConfigFieldMapping` has no type parameters now;
  - `ConfigFieldMapping` was replaced by `ProductHint` as the type of object to put in scope in order to customize
    the derivation of `ConfigConvert` for case class. Old `ConfigFieldMapping` implicit instances in scope have no
    effect now. The migration can be done by replacing code like
    `implicit def mapping: ConfigFieldMapping[T] = <mapping>` with
    `implicit def productHint: ProductHint[T] = ProductHint(<mapping>)`;
  - `ConfigConvert.fromString`, `ConfigConvert.fromNonEmptyString`, `ConfigConvert.vstringConvert`,
    `ConfigConvert.nonEmptyStringConvert` are now deprecated and the new helpers should be used instead.

### 0.5.1 (Jan 20, 2017)

- New features
  - More consistent handling of missing keys: if a config key is missing pureconfig always throws a
    `KeyNotFoundException` now, unless the `ConfigConvert` extends the new `AllowMissingKey` trait.
  - Add support for the `java.time` package. Converters types which support different string formats, such as `LocalDate`,
    must be configured before they can be used. See the [documentation](https://pureconfig.github.io/docs/configurable-converters.html)
    for more details.
  - Add support for converting objects with numeric keys into lists. This is a functionallity also supported
    by typesafe config since version [1.0.1](https://github.com/typesafehub/config/blob/f6680a5dad51d992139d45a84fad734f1778bf50/NEWS.md#101-may-19-2013)
    and discussed in the following [issue](https://github.com/typesafehub/config/issues/69).

### 0.5.0 (Jan 3, 2017)

- New features
  - Sealed families are now converted to and from configs unambiguously by using an extra `type` field (customizable) in
    their config representation; 
  - New `CoproductHint` trait which allows customization of the derived `ConfigConvert` for sealed families;
- Breaking changes
  - The default config representation for sealed families has changed:
    - By default pureconfig now expects to find a `type` field containing the lowercase simple class name of the type to
      be read. For example, for a family including `DogConf` and `CatConf`, pureconfig expects to find a
      `type: "dogconf"` field in the config file;
    - The old behavior can be restored by putting an implicit instance of `FirstSuccessCoproductHint` in scope (the
      migration to the new format is strongly recommended though, as the previous one may lead to ambiguous behavior);
    - More information about the default representation and on how to customize it can be seen in the
      [documentation](https://pureconfig.github.io/docs/override-behavior-for-sealed-families.html).
- Bug fixes
  - `0` is accepted again as a valid `Duration` in configs.
