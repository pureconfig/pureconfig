package pureconfig

import java.io.File
import java.net.URL
import java.nio.file.Path

import scala.reflect.ClassTag

import com.typesafe.config._
import pureconfig.ConfigReader.Result
import pureconfig.backend.{ ConfigFactoryWrapper, PathUtil }
import pureconfig.error.{ ConfigReaderException, ConfigReaderFailures }

trait ConfigSource { outer =>
  def value: Result[ConfigValue]

  def cursor: Result[ConfigCursor] =
    value.right.map(ConfigCursor(_, Nil))

  def fluentCursor: FluentConfigCursor =
    FluentConfigCursor(cursor)

  def at(namespace: String): ConfigSource =
    ConfigSource.fromCursor(fluentCursor.at(PathUtil.splitPath(namespace).map(p => p: PathSegment): _*))

  def load[A](implicit reader: Derivation[ConfigReader[A]]): Result[A] =
    cursor.right.flatMap(reader.value.from)

  @throws[ConfigReaderException[_]]
  def loadOrThrow[A: ClassTag](implicit reader: Derivation[ConfigReader[A]]): A = {
    load[A] match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[A](failures)
    }
  }
}

class ConfigObjectSource(readConfig: => Result[Config]) extends ConfigSource {
  private lazy val config: Result[Config] = readConfig
  lazy val value: Result[ConfigObject] = config.right.map(_.resolve.root)

  def withFallback(cs: ConfigObjectSource): ConfigObjectSource =
    new ConfigObjectSource(Result.zipWith(config, cs.config)(_.withFallback(_)))

  def withOptionalFallback(cs: ConfigObjectSource): ConfigObjectSource =
    withFallback(cs.recoverWith({ case _ => Right(ConfigFactory.empty) }))

  def recoverWith(f: PartialFunction[ConfigReaderFailures, Result[Config]]): ConfigObjectSource =
    new ConfigObjectSource(config.left.flatMap(f))
}

object ConfigObjectSource {
  def apply(conf: => Result[Config]): ConfigObjectSource = new ConfigObjectSource(conf)
  def fromCursor(cur: ConfigObjectCursor): ConfigObjectSource = new ConfigObjectSource(Right(cur.value.toConfig))
  def fromCursor(cur: FluentConfigCursor): ConfigObjectSource = new ConfigObjectSource(cur.asObjectCursor.right.map(_.value.toConfig))
}

object ConfigSource {
  val default = ConfigObjectSource(ConfigFactoryWrapper.load())
  val empty = ConfigObjectSource(Right(ConfigFactory.empty))

  val systemProperties = ConfigObjectSource(ConfigFactoryWrapper.systemProperties())
  val defaultReference = ConfigObjectSource(ConfigFactoryWrapper.defaultReference())
  val defaultApplication = ConfigObjectSource(ConfigFactoryWrapper.defaultApplication())

  val defaultReferenceUnresolved = ConfigObjectSource(ConfigFactoryWrapper.parseResources("reference.conf"))

  def file(path: String) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(new File(path)))
  def file(path: Path) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(path.toFile))
  def file(file: File) = ConfigObjectSource(ConfigFactoryWrapper.parseFile(file))

  def url(url: URL) = ConfigObjectSource(ConfigFactoryWrapper.parseURL(url))

  def resources(name: String) = ConfigObjectSource(ConfigFactoryWrapper.parseResources(name))

  def string(confStr: String) = ConfigObjectSource(ConfigFactoryWrapper.parseString(confStr))

  def fromConfig(conf: Config) = ConfigObjectSource(Right(conf))

  def fromCursor(cur: ConfigCursor): ConfigSource = new ConfigSource {
    lazy val value = Right(cur.value)
  }

  def fromCursor(cur: FluentConfigCursor): ConfigSource = new ConfigSource {
    lazy val value = cur.cursor.right.map(_.value)
  }
}
