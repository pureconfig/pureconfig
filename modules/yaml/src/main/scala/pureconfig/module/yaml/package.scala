package pureconfig.module

import java.io.{ IOException, Reader }
import java.nio.file.{ Files, Path }

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.{ Mark, MarkedYAMLException, YAMLException }
import pureconfig.ConvertHelpers._
import pureconfig._
import pureconfig.error._
import pureconfig.module.yaml.error.{ NonStringKeyFound, UnsupportedYamlType }

package object yaml {

  // Converts an object created by SnakeYAML to a Typesafe `ConfigValue`.
  // (https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-loading-yaml)
  private[this] def yamlObjToConfigValue(obj: AnyRef): Either[ConfigReaderFailures, ConfigValue] = {

    def aux(obj: AnyRef): Either[ConfigReaderFailures, AnyRef] = obj match {
      case m: java.util.Map[Object @unchecked, Object @unchecked] =>
        val entries = m.asScala.map {
          case (k: String, v) => aux(v).right.map { v: AnyRef => k -> v }
          case (k, _) => Left(ConfigReaderFailures(NonStringKeyFound(k.toString, k.getClass.getSimpleName)))
        }
        entries
          .foldLeft(Right(Map.empty): Either[ConfigReaderFailures, Map[String, AnyRef]])(combineResults(_, _)(_ + _))
          .right.map(_.asJava)

      case xs: java.util.List[Object @unchecked] =>
        xs.asScala.map(aux)
          .foldRight(Right(Nil): Either[ConfigReaderFailures, List[AnyRef]])(combineResults(_, _)(_ :: _))
          .right.map(_.asJava)

      case s: java.util.Set[Object @unchecked] =>
        s.asScala.map(aux)
          .foldLeft(Right(Set.empty): Either[ConfigReaderFailures, Set[AnyRef]])(combineResults(_, _)(_ + _))
          .right.map(_.asJava)

      case _: java.lang.Integer | _: java.lang.Long | _: java.lang.Double | _: java.lang.String | _: java.lang.Boolean =>
        Right(obj) // these types are supported directly by `ConfigValueFactory.fromAnyRef`

      case i: java.math.BigInteger =>
        Right(i.longValue(): java.lang.Long)

      case _: java.util.Date | _: java.sql.Date | _: java.sql.Timestamp =>
        Right(obj.toString)

      case ba: Array[Byte] =>
        Right(new String(ba))

      case null =>
        Right(null)

      case _ => // this shouldn't happen
        Left(ConfigReaderFailures(UnsupportedYamlType(obj.toString, obj.getClass.getSimpleName)))
    }

    aux(obj).right.map(ConfigValueFactory.fromAnyRef)
  }

  // Converts a SnakeYAML `Mark` to a `ConfigValueLocation`, provided the file path.
  private[this] def toConfigValueLocation(path: Path, mark: Mark): ConfigValueLocation = {
    ConfigValueLocation(path.toUri.toURL, mark.getLine + 1)
  }

  // Opens and processes a YAML file, converting all exceptions into the most appropriate PureConfig errors.
  private[this] def handleYamlErrors[A](path: Path)(f: Reader => Either[ConfigReaderFailures, A]): Either[ConfigReaderFailures, A] = {
    lazy val ioReader = Files.newBufferedReader(path)
    try f(ioReader)
    catch {
      case ex: IOException => Left(ConfigReaderFailures(CannotReadFile(path, Some(ex))))
      case ex: MarkedYAMLException => Left(ConfigReaderFailures(
        CannotParse(ex.getProblem, Some(toConfigValueLocation(path, ex.getProblemMark)))))
      case ex: YAMLException => Left(ConfigReaderFailures(CannotParse(ex.getMessage, None)))
      case NonFatal(ex) => Left(ConfigReaderFailures(ThrowableFailure(ex, None)))
    } finally {
      Try(ioReader.close())
    }
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the YAML file, else a `Failure` with details on why it isn't possible
   */
  def loadYaml[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    handleYamlErrors(path) { ioReader =>
      val yamlObj = new Yaml(new SafeConstructor()).load(ioReader)

      yamlObjToConfigValue(yamlObj).right.flatMap { cv =>
        reader.value.from(ConfigCursor(cv, Nil))
      }
    }
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadYamlOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    loadYaml(path) match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document YAML file. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the multi-document YAML file, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadYamls[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    handleYamlErrors(path) { ioReader =>
      val yamlObjs = new Yaml(new SafeConstructor()).loadAll(ioReader)

      yamlObjs.asScala.map(yamlObjToConfigValue)
        .foldRight(Right(Nil): Either[ConfigReaderFailures, List[AnyRef]])(combineResults(_, _)(_ :: _))
        .right.flatMap { cvs =>
          val cl = ConfigValueFactory.fromAnyRef(cvs.asJava)
          reader.value.from(ConfigCursor(cl, Nil))
        }
    }
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document YAML file. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadYamlsOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    loadYamls(path) match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }
}
