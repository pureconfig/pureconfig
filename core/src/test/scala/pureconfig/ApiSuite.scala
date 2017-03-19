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

}
