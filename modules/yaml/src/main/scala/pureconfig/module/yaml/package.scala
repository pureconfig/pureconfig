package pureconfig.module

import java.io.IOException
import java.nio.file.{ Files, Path }
import java.util.Base64

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.{ Mark, MarkedYAMLException, YAMLException }
import pureconfig._
import pureconfig.error._
import pureconfig.module.yaml.error.{ NonStringKeyFound, UnsupportedYamlType }

package object yaml {

  // Converts an object created by SnakeYAML to a Typesafe `ConfigValue`.
  // (https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-loading-yaml)
  private[this] def yamlObjToConfigValue(obj: AnyRef): ConfigReader.Result[ConfigValue] = {

    def aux(obj: AnyRef): ConfigReader.Result[AnyRef] = obj match {
      case m: java.util.Map[AnyRef @unchecked, AnyRef @unchecked] =>
        val entries = m.asScala.map {
          case (k: String, v) => aux(v).right.map { v: AnyRef => k -> v }
          case (k, _) => Left(ConfigReaderFailures(NonStringKeyFound(k.toString, k.getClass.getSimpleName)))
        }
        entries
          .foldLeft(Right(Map.empty): ConfigReader.Result[Map[String, AnyRef]])(ReaderResult.zipWith(_, _)(_ + _))
          .right.map(_.asJava)

      case xs: java.util.List[AnyRef @unchecked] =>
        xs.asScala.map(aux)
          .foldRight(Right(Nil): ConfigReader.Result[List[AnyRef]])(ReaderResult.zipWith(_, _)(_ :: _))
          .right.map(_.asJava)

      case s: java.util.Set[AnyRef @unchecked] =>
        s.asScala.map(aux)
          .foldLeft(Right(Set.empty): ConfigReader.Result[Set[AnyRef]])(ReaderResult.zipWith(_, _)(_ + _))
          .right.map(_.asJava)

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

  // Converts a SnakeYAML `Mark` to a `ConfigValueLocation`, provided the file path.
  private[this] def toConfigValueLocation(path: Path, mark: Mark): ConfigValueLocation = {
    ConfigValueLocation(path.toUri.toURL, mark.getLine + 1)
  }

  private[this] def using[A <: AutoCloseable, B](resource: => A)(f: A ⇒ B): B = {
    try f(resource)
    finally Try(resource.close())
  }

  // Opens and processes a YAML file, converting all exceptions into the most appropriate PureConfig errors.
  private[this] def handleYamlErrors[A](path: Option[Path])(block: => ConfigReader.Result[A]): ConfigReader.Result[A] = {
    try block
    catch {
      case ex: IOException if path.isDefined => Left(ConfigReaderFailures(CannotReadFile(path.get, Some(ex))))
      case ex: MarkedYAMLException => Left(ConfigReaderFailures(
        CannotParse(ex.getProblem, path.map(toConfigValueLocation(_, ex.getProblemMark)))))
      case ex: YAMLException => Left(ConfigReaderFailures(CannotParse(ex.getMessage, None)))
      case NonFatal(ex) => Left(ConfigReaderFailures(ThrowableFailure(ex, None)))
    }
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the YAML file, else a `Failure` with details on why it isn't possible
   */
  def loadYaml[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    handleYamlErrors(Some(path)) {
      using(Files.newBufferedReader(path)) { ioReader =>
        // we are using `SafeConstructor` in order to avoid creating custom Java instances, leaking the PureConfig
        // abstraction over SnakeYAML
        val yamlObj = new Yaml(new SafeConstructor()).load[AnyRef](ioReader)

        yamlObjToConfigValue(yamlObj).right.flatMap { cv =>
          reader.value.from(ConfigCursor(cv, Nil))
        }
      }
    }
  }

  /**
   * Loads a configuration of type `Config` from the given string.
   *
   * @param content the string containing the YAML document
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from `content`, else a `Failure` with details on why it isn't possible
   */
  def loadYaml[Config](content: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    handleYamlErrors(None) {
      // we are using `SafeConstructor` in order to avoid creating custom Java instances, leaking the PureConfig
      // abstraction over SnakeYAML
      val yamlObj = new Yaml(new SafeConstructor()).load[AnyRef](content)

      yamlObjToConfigValue(yamlObj).right.flatMap { cv =>
        reader.value.from(ConfigCursor(cv, Nil))
      }
    }
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
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
   * Loads a configuration of type `Config` from the given string.
   *
   * @param content the string containing the YAML document
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadYamlOrThrow[Config: ClassTag](content: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    loadYaml(content) match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document YAML file. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param path the path of the YAML file to read
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the multi-document YAML file, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadYamls[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    handleYamlErrors(Some(path)) {
      using(Files.newBufferedReader(path)) { ioReader =>
        // we are using `SafeConstructor` in order to avoid creating custom Java instances, leaking the PureConfig
        // abstraction over SnakeYAML
        val yamlObjs = new Yaml(new SafeConstructor()).loadAll(ioReader)

        yamlObjs.asScala.map(yamlObjToConfigValue)
          .foldRight(Right(Nil): ConfigReader.Result[List[AnyRef]])(ReaderResult.zipWith(_, _)(_ :: _))
          .right.flatMap { cvs =>
            val cl = ConfigValueFactory.fromAnyRef(cvs.asJava)
            reader.value.from(ConfigCursor(cl, Nil))
          }
      }
    }
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document string. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param content the string containing the YAML documents
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the multi-document string, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadYamls[Config](content: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    handleYamlErrors(None) {
      // we are using `SafeConstructor` in order to avoid creating custom Java instances, leaking the PureConfig
      // abstraction over SnakeYAML
      val yamlObjs = new Yaml(new SafeConstructor()).loadAll(content)

      yamlObjs.asScala.map(yamlObjToConfigValue)
        .foldRight(Right(Nil): ConfigReader.Result[List[AnyRef]])(ReaderResult.zipWith(_, _)(_ :: _))
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
   * @param path the path of the YAML file to read
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadYamlsOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    loadYamls(path) match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document string. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param content the string containing the YAML documents
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadYamlsOrThrow[Config: ClassTag](content: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    loadYamls(content) match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }
}
