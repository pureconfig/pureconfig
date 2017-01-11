### 0.5.1 (unreleased)

- New features
  - More consistent handling of missing keys: if a config key is missing pureconfig always throws a
    `KeyNotFoundException` now, unless the `ConfigConvert` extends the new `AllowMissingKey` trait.

### 0.5.0 (Jan 3, 2017)

- New features
  - Sealed families are now converted to and from configs unambiguously by using an extra `type` field (customizable) in
    their config representation; 
  - New `CoproductHint` trait which allows customization of the derived `ConfigConvert` for sealed families;
- Breaking changes
  - The default config representation for sealed families has changed. Configs for such classes that were read
    successfully in 0.4.x will fail to load in 0.5.x. The old behavior can be restored by putting an implicit instance
    of `FirstSuccessCoproductHint` in scope (the migration to the new format is strongly recommended though, as the
    previous one may lead to ambiguous behavior);
- Bug fixes
  - `0` is accepted again as a valid `Duration` in configs.
