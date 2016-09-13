package pureconfig

import org.scalatest.{ FlatSpec, Matchers }

class WordDelimiterTest extends FlatSpec with Matchers {
  "A CamelCaseWordDelimiter" should "properly tokenize words" in {
    CamelCaseWordDelimiter.toTokens("lowercase") shouldBe Seq("lowercase")
    CamelCaseWordDelimiter.toTokens("Class") shouldBe Seq("class")
    CamelCaseWordDelimiter.toTokens("MyClass") shouldBe Seq("my", "class")
    CamelCaseWordDelimiter.toTokens("HTML") shouldBe Seq("html")
    CamelCaseWordDelimiter.toTokens("PDFLoader") shouldBe Seq("pdf", "loader")
    CamelCaseWordDelimiter.toTokens("AString") shouldBe Seq("a", "string")
    CamelCaseWordDelimiter.toTokens("SimpleXMLParser") shouldBe Seq("simple", "xml", "parser")
    CamelCaseWordDelimiter.toTokens("GL11Version") shouldBe Seq("gl", "11", "version")
    CamelCaseWordDelimiter.toTokens("99Bottles") shouldBe Seq("99", "bottles")
    CamelCaseWordDelimiter.toTokens("May5") shouldBe Seq("may", "5")
    CamelCaseWordDelimiter.toTokens("BFG9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    CamelCaseWordDelimiter.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    CamelCaseWordDelimiter.fromTokens(Seq("class")) shouldBe "class"
    CamelCaseWordDelimiter.fromTokens(Seq("my", "class")) shouldBe "myClass"
    CamelCaseWordDelimiter.fromTokens(Seq("html")) shouldBe "html"
    CamelCaseWordDelimiter.fromTokens(Seq("pdf", "loader")) shouldBe "pdfLoader"
    CamelCaseWordDelimiter.fromTokens(Seq("a", "string")) shouldBe "aString"
    CamelCaseWordDelimiter.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simpleXmlParser"
    CamelCaseWordDelimiter.fromTokens(Seq("gl", "11", "version")) shouldBe "gl11Version"
    CamelCaseWordDelimiter.fromTokens(Seq("99", "bottles")) shouldBe "99Bottles"
    CamelCaseWordDelimiter.fromTokens(Seq("may", "5")) shouldBe "may5"
    CamelCaseWordDelimiter.fromTokens(Seq("bfg", "9000")) shouldBe "bfg9000"
  }
  "An HyphenWordDelimiter" should "properly tokenize words" in {
    HyphenWordDelimiter.toTokens("lowercase") shouldBe Seq("lowercase")
    HyphenWordDelimiter.toTokens("class") shouldBe Seq("class")
    HyphenWordDelimiter.toTokens("my-class") shouldBe Seq("my", "class")
    HyphenWordDelimiter.toTokens("html") shouldBe Seq("html")
    HyphenWordDelimiter.toTokens("pdf-loader") shouldBe Seq("pdf", "loader")
    HyphenWordDelimiter.toTokens("a-string") shouldBe Seq("a", "string")
    HyphenWordDelimiter.toTokens("simple-xml-parser") shouldBe Seq("simple", "xml", "parser")
    HyphenWordDelimiter.toTokens("gl-11-version") shouldBe Seq("gl", "11", "version")
    HyphenWordDelimiter.toTokens("99-bottles") shouldBe Seq("99", "bottles")
    HyphenWordDelimiter.toTokens("may-5") shouldBe Seq("may", "5")
    HyphenWordDelimiter.toTokens("bfg-9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    HyphenWordDelimiter.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    HyphenWordDelimiter.fromTokens(Seq("class")) shouldBe "class"
    HyphenWordDelimiter.fromTokens(Seq("my", "class")) shouldBe "my-class"
    HyphenWordDelimiter.fromTokens(Seq("html")) shouldBe "html"
    HyphenWordDelimiter.fromTokens(Seq("pdf", "loader")) shouldBe "pdf-loader"
    HyphenWordDelimiter.fromTokens(Seq("a", "string")) shouldBe "a-string"
    HyphenWordDelimiter.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simple-xml-parser"
    HyphenWordDelimiter.fromTokens(Seq("gl", "11", "version")) shouldBe "gl-11-version"
    HyphenWordDelimiter.fromTokens(Seq("99", "bottles")) shouldBe "99-bottles"
    HyphenWordDelimiter.fromTokens(Seq("may", "5")) shouldBe "may-5"
    HyphenWordDelimiter.fromTokens(Seq("bfg", "9000")) shouldBe "bfg-9000"
  }
  "An UnderscoreWordDelimiter" should "properly tokenize words" in {
    UnderscoreWordDelimiter.toTokens("lowercase") shouldBe Seq("lowercase")
    UnderscoreWordDelimiter.toTokens("class") shouldBe Seq("class")
    UnderscoreWordDelimiter.toTokens("my_class") shouldBe Seq("my", "class")
    UnderscoreWordDelimiter.toTokens("html") shouldBe Seq("html")
    UnderscoreWordDelimiter.toTokens("pdf_loader") shouldBe Seq("pdf", "loader")
    UnderscoreWordDelimiter.toTokens("a_string") shouldBe Seq("a", "string")
    UnderscoreWordDelimiter.toTokens("simple_xml_parser") shouldBe Seq("simple", "xml", "parser")
    UnderscoreWordDelimiter.toTokens("gl_11_version") shouldBe Seq("gl", "11", "version")
    UnderscoreWordDelimiter.toTokens("99_bottles") shouldBe Seq("99", "bottles")
    UnderscoreWordDelimiter.toTokens("may_5") shouldBe Seq("may", "5")
    UnderscoreWordDelimiter.toTokens("bfg_9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    UnderscoreWordDelimiter.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    UnderscoreWordDelimiter.fromTokens(Seq("class")) shouldBe "class"
    UnderscoreWordDelimiter.fromTokens(Seq("my", "class")) shouldBe "my_class"
    UnderscoreWordDelimiter.fromTokens(Seq("html")) shouldBe "html"
    UnderscoreWordDelimiter.fromTokens(Seq("pdf", "loader")) shouldBe "pdf_loader"
    UnderscoreWordDelimiter.fromTokens(Seq("a", "string")) shouldBe "a_string"
    UnderscoreWordDelimiter.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simple_xml_parser"
    UnderscoreWordDelimiter.fromTokens(Seq("gl", "11", "version")) shouldBe "gl_11_version"
    UnderscoreWordDelimiter.fromTokens(Seq("99", "bottles")) shouldBe "99_bottles"
    UnderscoreWordDelimiter.fromTokens(Seq("may", "5")) shouldBe "may_5"
    UnderscoreWordDelimiter.fromTokens(Seq("bfg", "9000")) shouldBe "bfg_9000"
  }
  "A NoWordDelimiter" should "not do anything when tokenizing words" in {
    NoWordDelimiter.toTokens("lowercase") shouldBe Seq("lowercase")
    NoWordDelimiter.toTokens("class") shouldBe Seq("class")
    NoWordDelimiter.toTokens("my-class") shouldBe Seq("my-class")
    NoWordDelimiter.toTokens("my_class") shouldBe Seq("my_class")
    NoWordDelimiter.toTokens("myClass") shouldBe Seq("myClass")
    NoWordDelimiter.toTokens("html") shouldBe Seq("html")
    NoWordDelimiter.toTokens("pdf-loader") shouldBe Seq("pdf-loader")
    NoWordDelimiter.toTokens("pdf_loader") shouldBe Seq("pdf_loader")
    NoWordDelimiter.toTokens("pdfLoader") shouldBe Seq("pdfLoader")
    NoWordDelimiter.toTokens("a-string") shouldBe Seq("a-string")
    NoWordDelimiter.toTokens("a_string") shouldBe Seq("a_string")
    NoWordDelimiter.toTokens("aString") shouldBe Seq("aString")
    NoWordDelimiter.toTokens("simple-xml-parser") shouldBe Seq("simple-xml-parser")
    NoWordDelimiter.toTokens("simple_xml_parser") shouldBe Seq("simple_xml_parser")
    NoWordDelimiter.toTokens("simpleXMLParser") shouldBe Seq("simpleXMLParser")
    NoWordDelimiter.toTokens("gl-11-version") shouldBe Seq("gl-11-version")
    NoWordDelimiter.toTokens("gl_11_version") shouldBe Seq("gl_11_version")
    NoWordDelimiter.toTokens("gl11Version") shouldBe Seq("gl11Version")
    NoWordDelimiter.toTokens("99-bottles") shouldBe Seq("99-bottles")
    NoWordDelimiter.toTokens("99_bottles") shouldBe Seq("99_bottles")
    NoWordDelimiter.toTokens("99Bottles") shouldBe Seq("99Bottles")
    NoWordDelimiter.toTokens("may-5") shouldBe Seq("may-5")
    NoWordDelimiter.toTokens("may_5") shouldBe Seq("may_5")
    NoWordDelimiter.toTokens("may5") shouldBe Seq("may5")
    NoWordDelimiter.toTokens("bfg-9000") shouldBe Seq("bfg-9000")
    NoWordDelimiter.toTokens("bfg_9000") shouldBe Seq("bfg_9000")
    NoWordDelimiter.toTokens("bfg9000") shouldBe Seq("bfg9000")
  }
  it should "join tokens without any special processing" in {
    NoWordDelimiter.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    NoWordDelimiter.fromTokens(Seq("class")) shouldBe "class"
    NoWordDelimiter.fromTokens(Seq("my-class")) shouldBe "my-class"
    NoWordDelimiter.fromTokens(Seq("my_class")) shouldBe "my_class"
    NoWordDelimiter.fromTokens(Seq("myClass")) shouldBe "myClass"
    NoWordDelimiter.fromTokens(Seq("html")) shouldBe "html"
    NoWordDelimiter.fromTokens(Seq("pdf-loader")) shouldBe "pdf-loader"
    NoWordDelimiter.fromTokens(Seq("pdf_loader")) shouldBe "pdf_loader"
    NoWordDelimiter.fromTokens(Seq("pdfLoader")) shouldBe "pdfLoader"
    NoWordDelimiter.fromTokens(Seq("a-string")) shouldBe "a-string"
    NoWordDelimiter.fromTokens(Seq("a_string")) shouldBe "a_string"
    NoWordDelimiter.fromTokens(Seq("aString")) shouldBe "aString"
    NoWordDelimiter.fromTokens(Seq("simple-xml-parser")) shouldBe "simple-xml-parser"
    NoWordDelimiter.fromTokens(Seq("simple_xml_parser")) shouldBe "simple_xml_parser"
    NoWordDelimiter.fromTokens(Seq("simpleXMLParser")) shouldBe "simpleXMLParser"
    NoWordDelimiter.fromTokens(Seq("gl-11-version")) shouldBe "gl-11-version"
    NoWordDelimiter.fromTokens(Seq("gl_11_version")) shouldBe "gl_11_version"
    NoWordDelimiter.fromTokens(Seq("gl11Version")) shouldBe "gl11Version"
    NoWordDelimiter.fromTokens(Seq("99-bottles")) shouldBe "99-bottles"
    NoWordDelimiter.fromTokens(Seq("99_bottles")) shouldBe "99_bottles"
    NoWordDelimiter.fromTokens(Seq("99Bottles")) shouldBe "99Bottles"
    NoWordDelimiter.fromTokens(Seq("may-5")) shouldBe "may-5"
    NoWordDelimiter.fromTokens(Seq("may_5")) shouldBe "may_5"
    NoWordDelimiter.fromTokens(Seq("may5")) shouldBe "may5"
    NoWordDelimiter.fromTokens(Seq("bfg-9000")) shouldBe "bfg-9000"
    NoWordDelimiter.fromTokens(Seq("bfg_9000")) shouldBe "bfg_9000"
    NoWordDelimiter.fromTokens(Seq("bfg9000")) shouldBe "bfg9000"
    NoWordDelimiter.fromTokens(Seq("bfg", "9000")) shouldBe "bfg9000"
  }
}
