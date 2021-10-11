package pureconfig.module.scalaxml

import scala.xml.Elem

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig.ConfigSource
import pureconfig.syntax._

class ScalaXMLSuite extends AnyFlatSpec with Matchers with EitherValues {

  val sampleXML: Elem =
    <people>
      <person firstName="foo" lastName="bar"/>
      <person firstName="blah" lastName="stuff"/>
    </people>

  it should "be able to read a config with XML" in {
    val source = ConfigSource.string(s"""{ people =
         |    \"\"\"$sampleXML\"\"\"
         | }""".stripMargin)
    source.at("people").load[Elem] shouldEqual Right(sampleXML)
  }

  it should "return an error when reading invalid XML " in {
    val source = ConfigSource.string("{ people: <people> }")
    source.at("people").load[Elem] shouldBe 'left
  }
}
