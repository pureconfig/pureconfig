package pureconfig.module.yaml

import java.io._
import java.net.{URI, URL}
import java.nio.file.{Files, Path, Paths}
import java.util.Base64

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.config.{ConfigOrigin, ConfigOriginFactory, ConfigValue, ConfigValueFactory}
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.{Mark, MarkedYAMLException, YAMLException}
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.{LoaderOptions, Yaml}

import pureconfig.ConfigReader.Result
import pureconfig.error._
import pureconfig.module.yaml.error.{NonStringKeyFound, UnsupportedYamlType}
import pureconfig.{ConfigObjectSource, ConfigSource}

/** A `ConfigSource` that reads configs from YAML documents in a stream, file or string.
  *
  * @param getReader
  *   the thunk to generate a `Reader` instance from which the YAML document will be read. This parameter won't be
  *   memoized so it can be used with dynamic sources (e.g. URLs)
  * @param uri
  *   the optional URI of the source. Used only to provide better error messages.
  * @param onIOFailure
  *   an optional function used to provide a custom failure when IO errors happen
  */
final class YamlConfigSource private (
    getReader: () => Reader,
    uri: Option[URI] = None,
    onIOFailure: Option[Option[Throwable] => CannotRead] = None
) extends ConfigSource {

  // instances of `Yaml` are not thread safe
  private[this] def loader = new Yaml(new CustomConstructor())

  def value(): Result[ConfigValue] = {
    usingReader { reader =>
      yamlObjToConfigValue(loader.load[AnyRef](reader))
    }
  }

  /** Converts this YAML source to a config object source to allow merging with other sources. This operation is not
    * reversible. The new source will load with an error if this document does not contain an object.
    *
    * @return
    *   a config object source that produces YAML object documents read by this source
    */
  def asObjectSource: ConfigObjectSource =
    ConfigObjectSource(fluentCursor().asObjectCursor.map(_.objValue.toConfig))

  /** Returns a new source that produces a multi-document YAML read by this source as a config list.
    *
    * @return
    *   a new source that produces a multi-document YAML read by this source as a config list.
    */
  def multiDoc: ConfigSource =
    new ConfigSource {
      def value(): Result[ConfigValue] = {
        usingReader { reader =>
          loader
            .loadAll(reader)
            .asScala
            .map(yamlObjToConfigValue)
            .foldRight(Right(Nil): Result[List[ConfigValue]])(Result.zipWith(_, _)(_ :: _))
            .map { cvs => ConfigValueFactory.fromIterable(cvs.asJava) }
        }
      }
    }

  // YAML has special support for timestamps and the built-in `SafeConstructor` parses values into Java `Date`
  // instances. However, date readers are expecting strings and the original format may matter to them. This class
  // specifies the string parser as the one to use for dates.
  private[this] class CustomConstructor extends SafeConstructor(new LoaderOptions()) {
    yamlConstructors.put(Tag.TIMESTAMP, new ConstructYamlStr())
  }

  // Converts an object created by SnakeYAML to a Typesafe `ConfigValue`.
  // (https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-loading-yaml)
  private[this] def yamlObjToConfigValue(obj: AnyRef): Result[ConfigValue] = {

    def aux(obj: AnyRef): Result[AnyRef] =
      obj match {
        case m: java.util.Map[AnyRef @unchecked, AnyRef @unchecked] =>
          val entries: Iterable[Result[(String, AnyRef)]] = m.asScala.map {
            case (k: String, v) => aux(v).map { (v: AnyRef) => k -> v }
            case (k, _) => Left(ConfigReaderFailures(NonStringKeyFound(k.toString, k.getClass.getSimpleName)))
          }
          Result.sequence(entries).map(_.toMap.asJava)

        case xs: java.util.List[AnyRef @unchecked] =>
          Result.sequence(xs.asScala.map(aux)).map(_.toList.asJava)

        case s: java.util.Set[AnyRef @unchecked] =>
          Result.sequence(s.asScala.map(aux)).map(_.toSet.asJava)

        case _: java.lang.Integer | _: java.lang.Long | _: java.lang.Double | _: java.lang.String |
            _: java.lang.Boolean =>
          Right(obj) // these types are supported directly by `ConfigValueFactory.fromAnyRef`

        case _: java.math.BigInteger =>
          Right(obj.toString)

        case ba: Array[Byte] =>
          Right(Base64.getEncoder.encodeToString(ba))

        case null =>
          Right(null)

        case _ => // this shouldn't happen
          Left(ConfigReaderFailures(UnsupportedYamlType(obj.toString, obj.getClass.getSimpleName)))
      }

    aux(obj).map(ConfigValueFactory.fromAnyRef)
  }

  // Opens and processes a YAML file, converting all exceptions into the most appropriate PureConfig errors.
  private[this] def usingReader[A](f: Reader => Result[A]): Result[A] = {
    try {
      val reader = getReader()
      try f(reader)
      finally Try(reader.close())
    } catch {
      case e: IOException if onIOFailure.nonEmpty =>
        Result.fail(onIOFailure.get(Some(e)))
      case e: MarkedYAMLException =>
        Result.fail(CannotParse(e.getProblem, uri.map { uri => toConfigOrigin(uri.toURL, e.getProblemMark) }))
      case e: YAMLException =>
        Result.fail(CannotParse(e.getMessage, None))
      case NonFatal(e) =>
        Result.fail(ThrowableFailure(e, None))
    }
  }

  // Converts a SnakeYAML `Mark` to a `ConfigOrigin`, provided the file path.
  private[this] def toConfigOrigin(path: URL, mark: Mark): ConfigOrigin = {
    ConfigOriginFactory.newURL(path).withLineNumber(mark.getLine + 1)
  }
}

object YamlConfigSource {

  /** Returns a YAML source that provides configs read from a file.
    *
    * @param path
    *   the path to the file as a string
    * @return
    *   a YAML source that provides configs read from a file.
    */
  def file(path: String) =
    new YamlConfigSource(
      () => new FileReader(path),
      uri = Some(new File(path).toURI),
      onIOFailure = Some(CannotReadFile(Paths.get(path), _))
    )

  /** Returns a YAML source that provides configs read from a file.
    *
    * @param path
    *   the path to the file
    * @return
    *   a YAML source that provides configs read from a file.
    */
  def file(path: Path) =
    new YamlConfigSource(
      () => Files.newBufferedReader(path),
      uri = Some(path.toUri),
      onIOFailure = Some(CannotReadFile(path, _))
    )

  /** Returns a YAML source that provides configs read from a file.
    *
    * @param file
    *   the file
    * @return
    *   a YAML source that provides configs read from a file.
    */
  def file(file: File) =
    new YamlConfigSource(
      () => new FileReader(file),
      uri = Some(file.toURI),
      onIOFailure = Some(CannotReadFile(file.toPath, _))
    )

  /** Returns a YAML source that provides a config parsed from a string.
    *
    * @param confStr
    *   the YAML content
    * @return
    *   a YAML source that provides a config parsed from a string.
    */
  def string(confStr: String) = new YamlConfigSource(() => new StringReader(confStr))
}
