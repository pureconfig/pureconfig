package pureconfig

import java.io.File
import java.net.URL
import java.nio.file.Path

import scala.reflect.ClassTag

import com.typesafe.config._

import pureconfig.ConfigReader.Result
import pureconfig.backend.ConfigWrapper._
import pureconfig.backend.{ConfigFactoryWrapper, PathUtil}
import pureconfig.error.{CannotRead, ConfigReaderException, ConfigReaderFailures}

/** A representation of a source from which `ConfigValue`s can be loaded, such as a file or a URL.
  *
  * A source allows users to load configs from this source as any type for which a `ConfigReader` is
  * available. Raw configs can also be retrieved as a `ConfigValue`, a `ConfigCursor` or a
  * `FluentConfigCursor`. Before using any of the loading methods described, Users can opt to focus
  * on a specific part of a config by specifying a namespace.
  *
  * All config loading methods are lazy and defer resolution of references until needed.
  */
trait ConfigSource {

  /** Retrieves a `ConfigValue` from this source. This forces the config to be resolved, if needed.
    *
    * @return a `ConfigValue` retrieved from this source.
    */
  def value(): Result[ConfigValue]

  /** Returns a cursor for a `ConfigValue` retrieved from this source.
    *
    * @return a cursor for a `ConfigValue` retrieved from this source.
    */
  def cursor(): Result[ConfigCursor] =
    value().map(ConfigCursor(_, Nil))

  /** Returns a fluent cursor for a `ConfigValue` retrieved from this source.
    *
    * @return a fluent cursor for a `ConfigValue` retrieved from this source.
    */
  def fluentCursor(): FluentConfigCursor =
    FluentConfigCursor(cursor())

  /** Navigates through the config to focus on a namespace.
    *
    * @param namespace the namespace to focus on
    * @return a new `ConfigSource` focused on the given namespace.
    */
  def at(namespace: String): ConfigSource =
    ConfigSource.fromCursor(fluentCursor().at(PathUtil.splitPath(namespace).map(p => p: PathSegment): _*))

  /** Loads a configuration of type `A` from this source.
    *
    * @tparam A the type of the config to be loaded
    * @return A `Right` with the configuration if it is possible to create an instance of type
    *         `A` from this source, a `Failure` with details on why it isn't possible otherwise
    */
  final def load[A](implicit reader: Derivation[ConfigReader[A]]): Result[A] =
    cursor().flatMap(reader.value.from)

  /** Loads a configuration of type `A` from this source. If it is not possible to create an
    * instance of `A`, this method throws a `ConfigReaderException`.
    *
    * @tparam A the type of the config to be loaded
    * @return The configuration of type `A` loaded from this source.
    */
  @throws[ConfigReaderException[_]]
  final def loadOrThrow[A: ClassTag](implicit reader: Derivation[ConfigReader[A]]): A = {
    load[A] match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[A](failures)
    }
  }
}

/** A `ConfigSource` which is guaranteed to generate config objects (maps) as root values.
  *
  * @param getConf the thunk to generate a `Config` instance. This parameter won't be memoized so it
  *                can be used with dynamic sources (e.g. URLs)
  */
final class ConfigObjectSource private (getConf: () => Result[Config]) extends ConfigSource {

  def value(): Result[ConfigObject] =
    config().flatMap(_.resolveSafe()).map(_.root)

  // Avoids unnecessary cast on `ConfigCursor#asObjectCursor`.
  override def cursor(): Result[ConfigCursor] =
    value().map(ConfigObjectCursor(_, Nil))

  /** Reads a `Config` from this config source. The returned config is usually unresolved, unless
    * the source forces it otherwise.
    *
    * @return a `Config` provided by this source.
    */
  def config(): Result[Config] =
    getConf()

  /** Merges this source with another one, with the latter being used as a fallback (e.g. the
    * source on which this method is called takes priority). Both sources are required to produce
    * a config object successfully.
    *
    * @param cs the config source to use as fallback
    * @return a new `ConfigObjectSource` that loads configs from both sources and uses `cs` as a
    *         fallback for this source
    */
  def withFallback(cs: ConfigObjectSource): ConfigObjectSource =
    ConfigObjectSource(Result.zipWith(config(), cs.config())(_.withFallback(_)))

  /** Returns a `ConfigObjectSource` that provides the same config as this one, but falls back to
    * providing an empty config when the source cannot be read. It can be used together with
    * `.withFallback` to specify optional config files to be merged (like `reference.conf`).
    *
    * @return a new `ConfigObjectSource` that provides the same config as this one, but falls back
    *         to an empty config if it cannot be read.
    */
  def optional: ConfigObjectSource =
    recoverWith { case ConfigReaderFailures(_: CannotRead) => ConfigSource.empty }

  /** Applies a function `f` if this source returns a failure, returning an alternative config
    * source in those cases.
    *
    * @param f the function to apply if this source returns a failure
    * @return a new `ConfigObjectSource` that provides an alternative config in case this source
    *         fails
    */
  def recoverWith(f: PartialFunction[ConfigReaderFailures, ConfigObjectSource]): ConfigObjectSource =
    ConfigObjectSource(getConf().left.flatMap { failures =>
      f.lift(failures) match {
        case None => Left(failures)
        case Some(source) => source.config()
      }
    })
}

object ConfigObjectSource {

  /** Creates a `ConfigObjectSource` from a `Result[Config]`. The provided argument is allowed
    * to change value over time.
    *
    * @param conf the config to be provided by this source
    * @return a `ConfigObjectSource` providing the given config.
    */
  def apply(conf: => Result[Config]): ConfigObjectSource =
    new ConfigObjectSource(() => conf)
}

/** Object containing factory methods for building `ConfigSource`s.
  *
  * The sources provided here use Typesafe Config configs created from files, resources, URLs or
  * strings. It also provides sources that delegate the loading component to Typesafe Config, to
  * leverage reference configs and overrides, making it easy to switch from using `ConfigFactory`
  * to `ConfigSource`.
  *
  * Other PureConfig modules may provide other ways or building config sources (e.g. for different
  * config formats or data sources).
  */
object ConfigSource {

  /** A config source for the default loading process in Typesafe Config. Typesafe Config stacks
    * `reference.conf` resources provided by libraries, application configs (by default
    * `application.conf` in resources) and system property overrides, resolves them and merges them
    * into a single config. This source is equivalent to
    * `defaultOverrides.withFallback(defaultApplication).withFallback(defaultReference)`.
    */
  val default = ConfigObjectSource(ConfigFactoryWrapper.load())

  /** A config source for the default loading process in Typesafe Config with a custom application
    * config source. Typesafe Config stacks `reference.conf` resources provided by libraries, the
    * given file and system property overrides, resolves them and merges them into a single config.
    *
    * This method is provided here to support use cases that previously depended on
    * `ConfigFactory.load(config)`. Creating a custom source by merging the layers manually is
    * usually recommended as it makes the config priorities more transparent.
    *
    * @param appSource the source providing the application config
    * @return a `ConfigObjectSource` for the default loading process in Typesafe Config with a
    *         custom application config source.
    */
  def default(appSource: ConfigObjectSource): ConfigObjectSource =
    ConfigObjectSource(appSource.config().flatMap(ConfigFactoryWrapper.load))

  /** A config source that always provides empty configs.
    */
  val empty = ConfigObjectSource(Right(ConfigFactory.empty))

  /** A config source for the default reference config in Typesafe Config (`reference.conf`
    * resources provided by libraries). Like Typesafe Config, it provides an empty object if
    * `reference.conf` files are not found.
    *
    * As required by
    * [[https://github.com/lightbend/config/blob/master/HOCON.md#conventional-configuration-files-for-jvm-apps the HOCON spec]],
    * the default reference files are pre-emptively resolved - substitutions in the reference config
    * aren't affected by application configs.
    */
  val defaultReference = ConfigObjectSource(ConfigFactoryWrapper.defaultReference())

  /** A config source for the default reference config in Typesafe Config (`reference.conf`
    * resources provided by libraries) before being resolved. This can be used as an alternative
    * to `defaultReference` for use cases that require `reference.conf` to depend on
    * `application.conf`. Like Typesafe Config, it provides an empty object if `reference.conf`
    * files are not found.
    */
  val defaultReferenceUnresolved = resources("reference.conf").optional

  /** A config source for the default application config in Typesafe Config (by default
    * `application.conf` in resources). Like Typesafe Config, it provides an empty object if
    * application config files are not found.
    */
  val defaultApplication = ConfigObjectSource(ConfigFactoryWrapper.defaultApplication())

  /** A config source for the default overrides in Typesafe Config (by default a map of system
    * properties).
    */
  val defaultOverrides = ConfigObjectSource(ConfigFactoryWrapper.defaultOverrides())

  /** A config source for Java system properties.
    */
  val systemProperties = ConfigObjectSource(ConfigFactoryWrapper.systemProperties())

  /** Returns a config source that provides configs read from a file.
    *
    * @param path the path to the file as a string
    * @return a config source that provides configs read from a file.
    */
  def file(path: String) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(new File(path)))

  /** Returns a config source that provides configs read from a file.
    *
    * @param path the path to the file
    * @return a config source that provides configs read from a file.
    */
  def file(path: Path) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(path.toFile))

  /** Returns a config source that provides configs read from a file.
    *
    * @param file the file
    * @return a config source that provides configs read from a file.
    */
  def file(file: File) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(file))

  /** Returns a config source that provides configs read from a URL. The URL can either point to a
    * local file or to a remote HTTP location.
    *
    * @param url the URL
    * @return a config source that provides configs read from a URL.
    */
  def url(url: URL) = ConfigObjectSource(ConfigFactoryWrapper.parseURL(url))

  /** Returns a config source that provides configs read from JVM resource files. If multiple files
    * are found, they are merged in no specific order. This method uses Typesafe Config's default
    * class loader (`Thread.currentThread().getContextClassLoader()`).
    *
    * @param name the resource name
    * @return a config source that provides configs read from JVM resource files.
    */
  def resources(name: String) =
    ConfigObjectSource(ConfigFactoryWrapper.parseResources(name, null))

  /** Returns a config source that provides configs read from JVM resource files. If multiple files
    * are found, they are merged in no specific order. The given class loader will be used to look
    * for resources.
    *
    * @param name the resource name
    * @param classLoader the class loader to use to look for resources
    * @return a config source that provides configs read from JVM resource files.
    */
  def resources(name: String, classLoader: ClassLoader) =
    ConfigObjectSource(ConfigFactoryWrapper.parseResources(name, classLoader))

  /** Returns a config source that provides a config parsed from a string.
    *
    * @param confStr the config content
    * @return a config source that provides a config parsed from a string.
    */
  def string(confStr: String) = ConfigObjectSource(ConfigFactoryWrapper.parseString(confStr))

  /** Returns a config source that provides a fixed `Config`.
    *
    * @param conf the config to be provided
    * @return a config source that provides the given config.
    */
  def fromConfig(conf: Config) = ConfigObjectSource(Right(conf))

  /** Creates a `ConfigSource` from a `ConfigCursor`.
    *
    * @param cur the cursor to be provided by this source
    * @return a `ConfigSource` providing the given cursor.
    */
  private[pureconfig] def fromCursor(cur: ConfigCursor): ConfigSource =
    new ConfigSource {
      def value(): Result[ConfigValue] = cur.asConfigValue
      override def cursor() = Right(cur)
    }

  /** Creates a `ConfigSource` from a `FluentConfigCursor`.
    *
    * @param cur the cursor to be provided by this source
    * @return a `ConfigSource` providing the given cursor.
    */
  private[pureconfig] def fromCursor(cur: FluentConfigCursor): ConfigSource =
    new ConfigSource {
      def value(): Result[ConfigValue] = cur.cursor.flatMap(_.asConfigValue)
      override def cursor() = cur.cursor
      override def fluentCursor() = cur
    }
}
