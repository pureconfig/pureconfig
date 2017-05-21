package pureconfig.module.scalaxml

import scala.xml.Elem

import com.typesafe.config.ConfigFactory.parseString
import org.scalatest._
import pureconfig.syntax._

class ScalaXMLSuite extends FlatSpec with Matchers with EitherValues {

  case class Config(people: Elem)

  val sampleXML: Elem =
    <people>
      <person firstName="foo" lastName="bar"/>
      <person firstName="blah" lastName="stuff"/>
    </people>

  it should "be able to read a config with XML" in {
    val config = parseString(
      s"""{ people =
         |    \"\"\"$sampleXML\"\"\"
         | }""".stripMargin)
    config.to[Config] shouldEqual Right(Config(sampleXML))
  }

  it should "return an error when reading invalid XML " in {
    val config = parseString("{ people: <people> }")
    config.to[Config] shouldBe 'left
  }
}
