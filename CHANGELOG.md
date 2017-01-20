### 0.5.1 (unreleased)

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
