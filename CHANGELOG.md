### 0.6.0 (unreleased)

- New features
  - New  `ProductHint` trait allowing customization of the derived `ConfigConvert` for case classes, superseeding
    `ConfigFieldMapping` ([docs](https://github.com/melrief/pureconfig#override-behaviour-for-case-classes)). In
    addition to defining field name mappings, `ProductHint` instances control:
    - Whether default field values should be used when
      fields are missing in the config ([docs](https://github.com/melrief/pureconfig#default-field-values));
    - Whether unknown keys are ignored or cause pureconfig to return a `Failure`
      ([docs](https://github.com/melrief/pureconfig#unknown-keys)).
- Breaking changes
  - The default field mapping changed from camel case config keys (e.g. `exampleKey`) to kebab case keys (e.g.
    `example-key`). Case class fields are still expected to be camel case. The old behavior can be retained by putting
    in scope an `implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))`;
  - `ConfigFieldMapping` has no type parameters now;
  - `ConfigFieldMapping` was replaced by `ProductHint` as the type of object to put in scope in order to customize
    the derivation of `ConfigConvert` for case class. Old `ConfigFieldMapping` implicit instances in scope have no
    effect now. The migration can be done by replacing code like
    `implicit def mapping: ConfigFieldMapping[T] = <mapping>` with
    `implicit def productHint: ProductHint[T] = ProductHint(<mapping>)`.

### 0.5.1 (Jan 20, 2017)

- New features
  - More consistent handling of missing keys: if a config key is missing pureconfig always throws a
    `KeyNotFoundException` now, unless the `ConfigConvert` extends the new `AllowMissingKey` trait.
  - Add support for the `java.time` package. Converters types which support different string formats, such as `LocalDate`,
    must be configured before they can be used. See the [README](https://github.com/melrief/pureconfig#configurable-converters)
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
      [README](https://github.com/melrief/pureconfig#override-behaviour-for-sealed-families).
- Bug fixes
  - `0` is accepted again as a valid `Duration` in configs.
