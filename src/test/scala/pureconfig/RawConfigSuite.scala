package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigValueType }
import org.scalatest._
import java.io.PrintWriter
import java.nio.file.Files

import pureconfig.conf.typesafeConfigToConfig

import PureconfSuite.withTempFile

class RawConfigSuite extends FlatSpec with Matchers {

  "typesafeConfigToConfig" should "be able to convert any Typesafe Config instance into a RawConfig" in {
    withTempFile { configFile =>

      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("object = { a: 43 }")
      writer.println("string = 'foobar'")
      writer.println("list = [1, 2, 3]")
      writer.println("number = 1")
      writer.println("boolean = true")
      writer.close()

      val config = ConfigFactory.parseFile(configFile.toFile)

      config.getValue("object").valueType shouldEqual ConfigValueType.OBJECT
      config.getValue("number").valueType shouldEqual ConfigValueType.NUMBER
      config.getValue("string").valueType shouldEqual ConfigValueType.STRING
      config.getValue("boolean").valueType shouldEqual ConfigValueType.BOOLEAN
      config.getValue("list").valueType shouldEqual ConfigValueType.LIST

      typesafeConfigToConfig(config) shouldEqual Map(
        "number" -> "1",
        "list" -> "1,2,3",
        "string" -> "'foobar'",
        "boolean" -> "true",
        "object.a" -> "43"
      )
    }
  }
}
