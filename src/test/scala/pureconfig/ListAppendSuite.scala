/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.net.URL
import java.io.File

import org.scalatest._

import PureconfSuite.fileList
import ListAppendSuite._
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigFactory

object ListAppendSuite {
  case class IntWrapper(i: Int)

  case class ConfWithListOfInt(list: List[IntWrapper])

  /**
   * A `ClassLoader` that only loads the specified resources, while allowing multiple URLs for a single name,
   * emulating the behavior of a classpath with matching files in multiple locations.
   *
   * Inspired loosely by Typesafe Config's `TestClassLoader` from ` TestUtils.scala`.
   */
  class ClosedUniverseClassLoader(resources: Seq[(String, URL)]) extends ClassLoader {
    val resourceMap: Map[String, Seq[URL]] = resources.groupBy(_._1).mapValues(_.map(_._2))
    override def findResources(name: String) = {
      resourceMap.get(name).getOrElse(Nil).toIterator.asJavaEnumeration
    }
    override def findResource(name: String) = {
      resourceMap.get(name).flatMap(_.headOption).getOrElse(null)
    }
  }

  def fileUrl(s: String): URL = new File(s).toURI.toURL

  def fileUrlList(names: String*): Seq[URL] = names.map(fileUrl).toVector

  def classLoaderWithMultipleApplicationConfs(applicationConfUrls: Seq[URL]): ClassLoader = {
    new ClosedUniverseClassLoader(applicationConfUrls.map("application.conf" -> _))
  }
}

class ListAppendSuite extends FlatSpec with Matchers with OptionValues with TryValues {
  private val symbolicAppendFiles = List(
    "src/test/resources/conf/loadConfigFromFiles/appendIntoList.priority1.conf",
    "src/test/resources/conf/loadConfigFromFiles/appendIntoList.priority2.conf"
  )
  private val symbolicAppendExpected = List(
    98, // These three surprise me; they appear first despite coming from the lower precedence file, rather than at the end of the list.
    99,
    100, // 98 and 100 are included via HOCON `include`. Their order within the file is preserved.
    8, // The next three come in order from the greater precedence file, as I'd expect.
    9,
    12,
    50 // This one is slurped in via a HOCON `include`.
  ).map(IntWrapper(_))

  private val verboseReversedAppendFiles = List(
    "src/test/resources/conf/loadConfigFromFiles/appendIntoListVerbosely.priority1.conf",
    "src/test/resources/conf/loadConfigFromFiles/appendIntoListVerbosely.priority2.conf",
    "src/test/resources/conf/loadConfigFromFiles/appendIntoListVerbosely.priority3.conf"
  )

  // By writing the config files with append reversed (i.e., `list = <values> ${?list}`) we can get the desired precedence and order.
  private val desiredExpected = List(
    1,
    2,
    99,
    100,
    404,
    808
  ).map(IntWrapper(_))

  "loadConfigFromFiles" should "load configurations which use += to append into a list from multiple files with some surprising results" in {
    val files = fileList(symbolicAppendFiles: _*)
    loadConfigFromFiles[ConfWithListOfInt](files).success.value shouldBe ConfWithListOfInt(symbolicAppendExpected)
  }

  it should "load list from multiple files with expected order of descending precedence using the verbose syntax in reverse order" in {
    val files = fileList(verboseReversedAppendFiles: _*)
    loadConfigFromFiles[ConfWithListOfInt](files).success.value shouldBe ConfWithListOfInt(desiredExpected)
  }

  "loadConfig" should "load configurations which use += to append into a list from multiple files with some surprising results" ignore {
    val config = ConfigFactory.load(classLoaderWithMultipleApplicationConfs(fileUrlList(symbolicAppendFiles: _*)))
    loadConfig[ConfWithListOfInt](config).success.value shouldBe ConfWithListOfInt(symbolicAppendExpected)
  }

  it should "load list from multiple files with expected order of descending precedence using the verbose syntax in reverse order" in {
    val config = ConfigFactory.load(classLoaderWithMultipleApplicationConfs(fileUrlList(verboseReversedAppendFiles: _*)))
    loadConfig[ConfWithListOfInt](config).success.value shouldBe ConfWithListOfInt(desiredExpected)
  }
}
