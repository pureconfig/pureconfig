package pureconfig.module.scalaxml

import scala.xml.Elem

import pureconfig.BaseSuite
import pureconfig.syntax._

class ScalaXMLSuite extends BaseSuite {

  val sampleXML: Elem =
    <people>
      <person firstName="foo" lastName="bar"/>
      <person firstName="blah" lastName="stuff"/>
    </people>

  it should "be able to read a config with XML" in {
    configValue(s"""\"\"\"$sampleXML\"\"\"""").to[Elem] shouldEqual Right(sampleXML)
  }

  it should "return an error when reading invalid XML" in {
    configValue("<people>").to[Elem] shouldBe 'left
  }
}
