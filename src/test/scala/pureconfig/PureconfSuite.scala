/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.io.PrintWriter
import java.nio.file.{ Path, Files }

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import shapeless.{ Coproduct, :+:, CNil }

import scala.util.{ Failure, Try, Success }

import org.scalatest._

/**
 * @author Mario Pastorelli
 */
object PureconfSuite {
  def withTempFile(f: Path => Unit): Unit = {
    val configFile = Files.createTempFile("pureconftest", ".property")
    f(configFile)
    Files.delete(configFile)
  }
}

import PureconfSuite._

class PureconfSuite extends FlatSpec with Matchers {

  // a simple "flat" configuration
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])

  "pureconfig" should s"be able to save and load ${classOf[FlatConfig]}" in {
    withTempFile { configFile =>
      val expectedConfig = FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6"))
      saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
      val config = loadConfig[FlatConfig](configFile)

      config should be(Success(expectedConfig))
    }
  }

  // a slightly more complex configuration
  implicit val dateStringConvert = new StringConvert[DateTime] {

    override def from(str: String): Try[DateTime] = Try(ISODateTimeFormat.dateTime().parseDateTime(str))
    override def to(t: DateTime): String = ISODateTimeFormat.dateTime().print(t)
  }

  type ConfigCoproduct = Float :+: Boolean :+: CNil
  case class Config(d: DateTime, l: List[Int], s: Set[Int], subConfig: FlatConfig, coproduct: ConfigCoproduct)

  it should s"be able to save and load ${classOf[Config]}" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")), Coproduct[ConfigCoproduct](false))
      saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile)

      config should be(Success(expectedConfig))
    }
  }

  // the same configuration but with custom namespace
  case class Config2(config: Config)

  it should s"be able to save ${classOf[Config2]} and load ${classOf[Config]} when namespace is set to config" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")), Coproduct[ConfigCoproduct](false))
      val configToSave = Config2(expectedConfig)
      saveConfigAsPropertyFile(configToSave, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile, "config")

      config should be(Success(expectedConfig))
    }
  }

  // a realistic example of configuration: common available Spark properties
  case class DriverConf(cores: Int, maxResultSize: String, memory: String)
  case class ExecutorConf(memory: String, extraJavaOptions: String)
  case class SparkAppConf(name: String)
  case class SparkLocalConf(dir: String)
  case class SparkConf(master: String, app: SparkAppConf, local: SparkLocalConf, driver: DriverConf, executor: ExecutorConf, extraListeners: Seq[String])
  case class SparkRootConf(spark: SparkConf)

  it should s"be able to save and load ${classOf[SparkRootConf]}" in {
    withTempFile { configFile =>

      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("""spark.executor.extraJavaOptions=""""")
      writer.println("""spark.driver.maxResultSize="2g"""")
      writer.println("""spark.extraListeners=""""")
      writer.println("""spark.app.name="myApp"""")
      writer.println("""spark.driver.memory="1g"""")
      writer.println("""spark.driver.cores="10"""")
      writer.println("""spark.master="local[*]"""")
      writer.println("""spark.executor.memory="2g"""")
      writer.println("""spark.local.dir="/tmp/"""")
      // unused configuration
      writer.println("""akka.loggers = ["akka.event.Logging$DefaultLogger"]""")
      writer.close()

      val configOrError = loadConfig[SparkRootConf](configFile)

      val config = configOrError match {
        case Failure(f) => fail(f)
        case Success(c) => c
      }

      config.spark.executor.extraJavaOptions should be("")
      config.spark.driver.maxResultSize should be("2g")
      config.spark.extraListeners should be(Seq.empty[String])
      config.spark.app.name should be("myApp")
      config.spark.driver.memory should be("1g")
      config.spark.driver.cores should be(10)
      config.spark.master should be("local[*]")
      config.spark.executor.memory should be("2g")
      config.spark.local.dir should be("/tmp/")
    }
  }

  case class MapConf(conf: Map[String, Int])

  it should s"be able to save and load Map like fields" in {
    withTempFile { configFile =>
      val expected = MapConf(Map("a" -> 1, "b" -> 2, "c" -> 3))
      saveConfigAsPropertyFile(expected, configFile, overrideOutputPath = true)
      val result = loadConfig[MapConf](configFile)

      result shouldEqual (Success(expected))
    }
  }

  it should s"not be able to load invalid Maps" in {
    // invalid value
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a=foo")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isFailure shouldEqual true
    }

    // invalid key because it contains the namespace separator
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a.b=1")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isFailure shouldEqual true
    }
  }

}
