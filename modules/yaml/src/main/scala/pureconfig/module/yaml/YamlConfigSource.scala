package pureconfig.module.yaml

import java.io._
import java.net.{ URI, URL }
import java.nio.file.{ Files, Path, Paths }
import java.util.Base64

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.{ Mark, MarkedYAMLException, YAMLException }
import pureconfig.ConfigReader.Result
import pureconfig.error._
import pureconfig.module.yaml.error.{ NonStringKeyFound, UnsupportedYamlType }
import pureconfig.{ ConfigObjectSource, ConfigSource }

final class YamlConfigSource private (
    getReader: () => Reader,
    uri: Option[URI] = None,
    onIOFailure: Option[Option[Throwable] => CannotRead] = None) extends ConfigSource {

  private[this] val loader = new Yaml(new SafeConstructor())

  def value(): Result[ConfigValue] = {
    usingReader { reader =>
      yamlObjToConfigValue(loader.load[AnyRef](reader))
    }
  }

  def asObjectSource: ConfigObjectSource =
    ConfigObjectSource(fluentCursor().asObjectCursor.right.map(_.value.toConfig))

  def multiDoc: ConfigSource = new ConfigSource {
    def value(): Result[ConfigValue] = {
      usingReader { reader =>
        loader.loadAll(reader).asScala
          .map(yamlObjToConfigValue)
          .foldRight(Right(Nil): Result[List[ConfigValue]])(Result.zipWith(_, _)(_ :: _))
          .right.map { cvs => ConfigValueFactory.fromIterable(cvs.asJava) }
      }
    }
  }

  // Converts an object created by SnakeYAML to a Typesafe `ConfigValue`.
  // (https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-loading-yaml)
  private[this] def yamlObjToConfigValue(obj: AnyRef): Result[ConfigValue] = {

    def aux(obj: AnyRef): Result[AnyRef] = obj match {
      case m: java.util.Map[AnyRef @unchecked, AnyRef @unchecked] =>
        val entries: Iterable[Result[(String, AnyRef)]] = m.asScala.map {
          case (k: String, v) => aux(v).right.map { v: AnyRef => k -> v }
          case (k, _) => Left(ConfigReaderFailures(NonStringKeyFound(k.toString, k.getClass.getSimpleName)))
        }
        Result.sequence(entries).right.map(_.toMap.asJava)

      case xs: java.util.List[AnyRef @unchecked] =>
        Result.sequence(xs.asScala.map(aux)).right.map(_.toList.asJava)

      case s: java.util.Set[AnyRef @unchecked] =>
        Result.sequence(s.asScala.map(aux)).right.map(_.toSet.asJava)

      case _: java.lang.Integer | _: java.lang.Long | _: java.lang.Double | _: java.lang.String | _: java.lang.Boolean =>
        Right(obj) // these types are supported directly by `ConfigValueFactory.fromAnyRef`

      case _: java.util.Date | _: java.sql.Date | _: java.sql.Timestamp | _: java.math.BigInteger =>
        Right(obj.toString)

      case ba: Array[Byte] =>
        Right(Base64.getEncoder.encodeToString(ba))

      case null =>
        Right(null)

      case _ => // this shouldn't happen
        Left(ConfigReaderFailures(UnsupportedYamlType(obj.toString, obj.getClass.getSimpleName)))
    }

    aux(obj).right.map(ConfigValueFactory.fromAnyRef)
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
        Result.fail(CannotParse(e.getProblem, uri.map { uri => toConfigValueLocation(uri.toURL, e.getProblemMark) }))
      case e: YAMLException =>
        Result.fail(CannotParse(e.getMessage, None))
      case NonFatal(e) =>
        Result.fail(ThrowableFailure(e, None))
    }
  }

  // Converts a SnakeYAML `Mark` to a `ConfigValueLocation`, provided the file path.
  private[this] def toConfigValueLocation(path: URL, mark: Mark): ConfigValueLocation = {
    ConfigValueLocation(path, mark.getLine + 1)
  }
}

object YamlConfigSource {

  /**
   * Returns a YAML source that provides configs read from a file.
   *
   * @param path the path to the file as a string
   * @return a YAML source that provides configs read from a file.
   */
  def file(path: String) = new YamlConfigSource(
    () => new FileReader(path),
    uri = Some(new File(path).toURI),
    onIOFailure = Some(CannotReadFile(Paths.get(path), _)))

  /**
   * Returns a YAML source that provides configs read from a file.
   *
   * @param path the path to the file
   * @return a YAML source that provides configs read from a file.
   */
  def file(path: Path) = new YamlConfigSource(
    () => Files.newBufferedReader(path),
    uri = Some(path.toUri),
    onIOFailure = Some(CannotReadFile(path, _)))

  /**
   * Returns a YAML source that provides configs read from a file.
   *
   * @param file the file
   * @return a YAML source that provides configs read from a file.
   */
  def file(file: File) = new YamlConfigSource(
    () => new FileReader(file),
    uri = Some(file.toURI),
    onIOFailure = Some(CannotReadFile(file.toPath, _)))

  /**
   * Returns a YAML source that provides a config parsed from a string.
   *
   * @param confStr the config content
   * @return a YAML source that provides a config parsed from a string.
   */
  def string(confStr: String) = new YamlConfigSource(
    () => new StringReader(confStr))
}
