package pureconfig

import com.typesafe.config._
import pureconfig.error._

class FluentConfigCursorSuite extends BaseSuite {

  val defaultPath = List("key2", "key1")
  val defaultPathStr = "key1.key2"

  def conf(confStr: String): ConfigValue = {
    ConfigFactory.parseString(s"aux = $confStr").root.get("aux")
  }

  def cursor(confStr: String, pathElems: List[String] = defaultPath): FluentConfigCursor =
    ConfigCursor(conf(confStr), pathElems).fluent

  def failedCursor(reason: FailureReason, path: String, origin: Option[ConfigOrigin] = stringConfigOrigin(1)): FluentConfigCursor =
    FluentConfigCursor(Left(ConfigReaderFailures(ConvertFailure(reason, origin, path), Nil)))

  behavior of "FluentConfigCursor"

  it should "provide a method to cast its value to primitive types" in {
    cursor("abc").asString shouldBe Right("abc")
    cursor("5").asInt shouldBe Right(5)
    cursor("true").asBoolean shouldBe Right(true)
  }

  it should "allow being casted to a list cursor in a safe way" in {
    cursor("abc").asListCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST)), defaultPathStr, stringConfigOrigin(1))

    cursor("[1, 2]").asListCursor shouldBe
      Right(ConfigListCursor(conf("[1, 2]").asInstanceOf[ConfigList], defaultPath))
  }

  it should "allow being casted to an object cursor in a safe way" in {
    cursor("abc").asObjectCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)), defaultPathStr, stringConfigOrigin(1))

    cursor("{ a: 1, b: 2 }").asObjectCursor shouldBe
      Right(ConfigObjectCursor(conf("{ a: 1, b: 2 }").asInstanceOf[ConfigObject], defaultPath))
  }

  it should "allow access to a given path with implicit failure handling" in {
    cursor("{ a: 2 }").at("a") shouldBe
      cursor("2", "a" :: defaultPath)

    cursor("{ a: { b: 2 } }").at("a", "b") shouldBe
      cursor("2", "b" :: "a" :: defaultPath)

    cursor("{ a: { b: [{ c: 2 }] } }").at("a", "b", 0, "c") shouldBe
      cursor("2", "c" :: "0" :: "b" :: "a" :: defaultPath)

    cursor("{ a: { b: { c: 2 } } }").at("a", "c") shouldBe
      failedCursor(KeyNotFound("c", Set()), s"$defaultPathStr.a")

    cursor("{ a: { b: { c: 2 } } }").at("a", "c", "d") shouldBe
      failedCursor(KeyNotFound("c", Set()), s"$defaultPathStr.a")

    cursor("{ a: { b: [{ c: 2 }] } }").at("a", "b", 5, "c") shouldBe
      failedCursor(KeyNotFound("5", Set()), s"$defaultPathStr.a.b")
  }

  it should "allow for config list mapping with error aggregation" in {
    cursor("[1, 2, 3]").mapList(_.asInt) shouldBe Right(List(1, 2, 3))

    cursor("[true, notTrue, false, nay]").mapList(_.asBoolean) shouldBe Left(
      ConfigReaderFailures(
        ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.BOOLEAN)), stringConfigOrigin(1), s"$defaultPathStr.1"),
        ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.BOOLEAN)), stringConfigOrigin(1), s"$defaultPathStr.3") :: Nil))

    cursor("abc").mapList(_.asInt) should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST)), defaultPathStr, stringConfigOrigin(1))
  }

  it should "allow for config object mapping with error aggregation" in {
    cursor("{ a: abc, b: def }").mapObject(_.asString) shouldBe Right(Map("a" -> "abc", "b" -> "def"))

    cursor("{ a: abc, b: 5, c: [6] }").mapObject(_.asInt) shouldBe Left(
      ConfigReaderFailures(
        ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), stringConfigOrigin(1), s"$defaultPathStr.a"),
        ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.NUMBER)), stringConfigOrigin(1), s"$defaultPathStr.c") :: Nil))

    cursor("abc").mapObject(_.asInt) should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)), defaultPathStr, stringConfigOrigin(1))
  }
}
