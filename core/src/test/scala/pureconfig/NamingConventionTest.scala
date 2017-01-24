package pureconfig

import org.scalatest.{FlatSpec, Matchers}

class NamingConventionTest extends FlatSpec with Matchers {
  "CamelCase" should "properly tokenize words" in {
    CamelCase.toTokens("lowercase") shouldBe Seq("lowercase")
    CamelCase.toTokens("Class") shouldBe Seq("class")
    CamelCase.toTokens("MyClass") shouldBe Seq("my", "class")
    CamelCase.toTokens("HTML") shouldBe Seq("html")
    CamelCase.toTokens("PDFLoader") shouldBe Seq("pdf", "loader")
    CamelCase.toTokens("AString") shouldBe Seq("a", "string")
    CamelCase.toTokens("SimpleXMLParser") shouldBe Seq("simple", "xml", "parser")
    CamelCase.toTokens("GL11Version") shouldBe Seq("gl", "11", "version")
    CamelCase.toTokens("99Bottles") shouldBe Seq("99", "bottles")
    CamelCase.toTokens("May5") shouldBe Seq("may", "5")
    CamelCase.toTokens("BFG9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    CamelCase.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    CamelCase.fromTokens(Seq("class")) shouldBe "class"
    CamelCase.fromTokens(Seq("my", "class")) shouldBe "myClass"
    CamelCase.fromTokens(Seq("html")) shouldBe "html"
    CamelCase.fromTokens(Seq("pdf", "loader")) shouldBe "pdfLoader"
    CamelCase.fromTokens(Seq("a", "string")) shouldBe "aString"
    CamelCase.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simpleXmlParser"
    CamelCase.fromTokens(Seq("gl", "11", "version")) shouldBe "gl11Version"
    CamelCase.fromTokens(Seq("99", "bottles")) shouldBe "99Bottles"
    CamelCase.fromTokens(Seq("may", "5")) shouldBe "may5"
    CamelCase.fromTokens(Seq("bfg", "9000")) shouldBe "bfg9000"
  }
  "PascalCase" should "properly tokenize words" in {
    PascalCase.toTokens("lowercase") shouldBe Seq("lowercase")
    PascalCase.toTokens("Class") shouldBe Seq("class")
    PascalCase.toTokens("MyClass") shouldBe Seq("my", "class")
    PascalCase.toTokens("HTML") shouldBe Seq("html")
    PascalCase.toTokens("PDFLoader") shouldBe Seq("pdf", "loader")
    PascalCase.toTokens("AString") shouldBe Seq("a", "string")
    PascalCase.toTokens("SimpleXMLParser") shouldBe Seq("simple", "xml", "parser")
    PascalCase.toTokens("GL11Version") shouldBe Seq("gl", "11", "version")
    PascalCase.toTokens("99Bottles") shouldBe Seq("99", "bottles")
    PascalCase.toTokens("May5") shouldBe Seq("may", "5")
    PascalCase.toTokens("BFG9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    PascalCase.fromTokens(Seq("lowercase")) shouldBe "Lowercase"
    PascalCase.fromTokens(Seq("class")) shouldBe "Class"
    PascalCase.fromTokens(Seq("my", "class")) shouldBe "MyClass"
    PascalCase.fromTokens(Seq("html")) shouldBe "Html"
    PascalCase.fromTokens(Seq("pdf", "loader")) shouldBe "PdfLoader"
    PascalCase.fromTokens(Seq("a", "string")) shouldBe "AString"
    PascalCase.fromTokens(Seq("simple", "xml", "parser")) shouldBe "SimpleXmlParser"
    PascalCase.fromTokens(Seq("gl", "11", "version")) shouldBe "Gl11Version"
    PascalCase.fromTokens(Seq("99", "bottles")) shouldBe "99Bottles"
    PascalCase.fromTokens(Seq("may", "5")) shouldBe "May5"
    PascalCase.fromTokens(Seq("bfg", "9000")) shouldBe "Bfg9000"
  }
  "KebabCase" should "properly tokenize words" in {
    KebabCase.toTokens("lowercase") shouldBe Seq("lowercase")
    KebabCase.toTokens("class") shouldBe Seq("class")
    KebabCase.toTokens("my-class") shouldBe Seq("my", "class")
    KebabCase.toTokens("html") shouldBe Seq("html")
    KebabCase.toTokens("pdf-loader") shouldBe Seq("pdf", "loader")
    KebabCase.toTokens("a-string") shouldBe Seq("a", "string")
    KebabCase.toTokens("simple-xml-parser") shouldBe Seq("simple", "xml", "parser")
    KebabCase.toTokens("gl-11-version") shouldBe Seq("gl", "11", "version")
    KebabCase.toTokens("99-bottles") shouldBe Seq("99", "bottles")
    KebabCase.toTokens("may-5") shouldBe Seq("may", "5")
    KebabCase.toTokens("bfg-9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    KebabCase.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    KebabCase.fromTokens(Seq("class")) shouldBe "class"
    KebabCase.fromTokens(Seq("my", "class")) shouldBe "my-class"
    KebabCase.fromTokens(Seq("html")) shouldBe "html"
    KebabCase.fromTokens(Seq("pdf", "loader")) shouldBe "pdf-loader"
    KebabCase.fromTokens(Seq("a", "string")) shouldBe "a-string"
    KebabCase.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simple-xml-parser"
    KebabCase.fromTokens(Seq("gl", "11", "version")) shouldBe "gl-11-version"
    KebabCase.fromTokens(Seq("99", "bottles")) shouldBe "99-bottles"
    KebabCase.fromTokens(Seq("may", "5")) shouldBe "may-5"
    KebabCase.fromTokens(Seq("bfg", "9000")) shouldBe "bfg-9000"
  }
  "SnakeCase" should "properly tokenize words" in {
    SnakeCase.toTokens("lowercase") shouldBe Seq("lowercase")
    SnakeCase.toTokens("class") shouldBe Seq("class")
    SnakeCase.toTokens("my_class") shouldBe Seq("my", "class")
    SnakeCase.toTokens("html") shouldBe Seq("html")
    SnakeCase.toTokens("pdf_loader") shouldBe Seq("pdf", "loader")
    SnakeCase.toTokens("a_string") shouldBe Seq("a", "string")
    SnakeCase.toTokens("simple_xml_parser") shouldBe Seq("simple", "xml", "parser")
    SnakeCase.toTokens("gl_11_version") shouldBe Seq("gl", "11", "version")
    SnakeCase.toTokens("99_bottles") shouldBe Seq("99", "bottles")
    SnakeCase.toTokens("may_5") shouldBe Seq("may", "5")
    SnakeCase.toTokens("bfg_9000") shouldBe Seq("bfg", "9000")
  }
  it should "properly combine tokens" in {
    SnakeCase.fromTokens(Seq("lowercase")) shouldBe "lowercase"
    SnakeCase.fromTokens(Seq("class")) shouldBe "class"
    SnakeCase.fromTokens(Seq("my", "class")) shouldBe "my_class"
    SnakeCase.fromTokens(Seq("html")) shouldBe "html"
    SnakeCase.fromTokens(Seq("pdf", "loader")) shouldBe "pdf_loader"
    SnakeCase.fromTokens(Seq("a", "string")) shouldBe "a_string"
    SnakeCase.fromTokens(Seq("simple", "xml", "parser")) shouldBe "simple_xml_parser"
    SnakeCase.fromTokens(Seq("gl", "11", "version")) shouldBe "gl_11_version"
    SnakeCase.fromTokens(Seq("99", "bottles")) shouldBe "99_bottles"
    SnakeCase.fromTokens(Seq("may", "5")) shouldBe "may_5"
    SnakeCase.fromTokens(Seq("bfg", "9000")) shouldBe "bfg_9000"
  }
}
