/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.nio.file.{ Files, Path }

import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.collection.immutable._

/**
 * @author Mario Pastorelli
 */
object ApiSuite {
  def withTempFile(f: Path => Unit): Unit = {
    val configFile = Files.createTempFile("pureconftest", ".property")
    f(configFile)
    Files.delete(configFile)
  }

  def fileList(names: String*): Seq[java.io.File] = {
    names.map(new java.io.File(_)).toVector
  }
}

import pureconfig.ApiSuite._

class ApiSuite extends FlatSpec with Matchers with OptionValues with EitherValues with PropertyChecks {

  // checks if saving and loading a configuration from file returns the configuration itself
  def saveAndLoadIsIdentity[C](config: C)(implicit configConvert: ConfigConvert[C]): Unit = {
    withTempFile { configFile =>
      saveConfigAsPropertyFile(config, configFile, overrideOutputPath = true)
      loadConfig[C](configFile) shouldEqual Right(config)
    }
  }

  //  // api
  //  "loadConfigFromFiles" should "load a complete configuration from a single file" in {
  //    val files = fileList(
  //      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
  //    loadConfigFromFiles[FlatConfig](files).right.get shouldBe expectedValueForResolveFilesPriority2
  //  }
  //
  //  // api
  //  "loadConfigWithFallBack" should "fallback if no config keys are found" in {
  //    val priority1Conf = ConfigFactory.load("conf/loadConfigFromFiles/priority1.conf")
  //    val actual = loadConfigWithFallback[FlatConfig](priority1Conf)
  //    actual.right.get shouldBe FlatConfig(
  //      true,
  //      0.0d,
  //      0.99f,
  //      0,
  //      1L,
  //      "default",
  //      None)
  //  }
  //
  //  // api
  //  it should "fill in missing values from the lower priority files" in {
  //    val files = fileList(
  //      "core/src/test/resources/conf/loadConfigFromFiles/priority1.conf",
  //      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
  //    val actual = loadConfigFromFiles[FlatConfig](files)
  //    actual.right.get shouldBe FlatConfig(
  //      true,
  //      0.001d,
  //      0.99f,
  //      42,
  //      1L,
  //      "cheese",
  //      None // Notice that a Some in a lower priority file does not override a None.
  //    )
  //  }
  //
  //  // api
  //  it should "complain if the configuration is incomplete" in {
  //    val files = fileList(
  //      "core/src/test/resources/conf/loadConfigFromFiles/priority1.conf")
  //    val actual = loadConfigFromFiles[FlatConfig](files)
  //    actual.isLeft shouldBe true
  //  }
  //
  //  // api
  //  it should "silently ignore files which can't be read" in {
  //    val files = fileList(
  //      "core/src/test/resources/conf/loadConfigFromFiles/this.is.not.a.conf",
  //      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
  //    loadConfigFromFiles[FlatConfig](files).right.value shouldBe expectedValueForResolveFilesPriority2
  //  }
  //
  //  // api
  //  it should "complain if the list of files is empty" in {
  //    val files = fileList()
  //    loadConfigFromFiles[FlatConfig](files) shouldBe a[Left[_, _]]
  //  }

  //  it should s"be able to save ${classOf[Config2]} and load ${classOf[Config]} when namespace is set to config" in {
  //    withTempFile { configFile =>
  //      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")))
  //      val configToSave = Config2(expectedConfig)
  //      saveConfigAsPropertyFile(configToSave, configFile, overrideOutputPath = true)
  //      val config = loadConfig[Config](configFile, "config")
  //
  //      config should be(Right(expectedConfig))
  //    }
  //  }
  //
  // a realistic example of configuration: common available Spark properties
  //  case class DriverConf(cores: Int, maxResultSize: String, memory: String)
  //  case class ExecutorConf(memory: String, extraJavaOptions: String)
  //  case class SparkAppConf(name: String)
  //  case class SparkLocalConf(dir: String)
  //  case class SparkNetwork(timeout: FiniteDuration)
  //  case class SparkConf(master: String, app: SparkAppConf, local: SparkLocalConf, driver: DriverConf, executor: ExecutorConf, extraListeners: Seq[String], network: SparkNetwork)
  //  case class SparkRootConf(spark: SparkConf)

  //  // ** save and load, product
  //  it should s"be able to save and load ${classOf[SparkRootConf]}" in {
  //    withTempFile { configFile =>
  //
  //      val writer = new PrintWriter(Files.newOutputStream(configFile))
  //      writer.println("""spark.executor.extraJavaOptions=""""")
  //      writer.println("""spark.driver.maxResultSize="2g"""")
  //      writer.println("""spark.extraListeners=[]""")
  //      writer.println("""spark.app.name="myApp"""")
  //      writer.println("""spark.driver.memory="1g"""")
  //      writer.println("""spark.driver.cores="10"""")
  //      writer.println("""spark.master="local[*]"""")
  //      writer.println("""spark.executor.memory="2g"""")
  //      writer.println("""spark.local.dir="/tmp/"""")
  //      writer.println("""spark.network.timeout=45s""")
  //      // unused configuration
  //      writer.println("""akka.loggers = ["akka.event.Logging$DefaultLogger"]""")
  //      writer.close()
  //
  //      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  //      val configOrError = loadConfig[SparkRootConf](configFile)
  //
  //      val config = configOrError match {
  //        case Left(f) => fail(f.toString)
  //        case Right(c) => c
  //      }
  //
  //      config.spark.executor.extraJavaOptions should be("")
  //      config.spark.driver.maxResultSize should be("2g")
  //      config.spark.extraListeners should be(Seq.empty[String])
  //      config.spark.app.name should be("myApp")
  //      config.spark.driver.memory should be("1g")
  //      config.spark.driver.cores should be(10)
  //      config.spark.master should be("local[*]")
  //      config.spark.executor.memory should be("2g")
  //      config.spark.local.dir should be("/tmp/")
  //      config.spark.network.timeout should be(FiniteDuration(45, TimeUnit.SECONDS))
  //    }
  //  }
}
