package pureconfig

import com.typesafe.config._
import pureconfig.error.{ KeyNotFound, WrongType }

class ConfigCursorSuite extends BaseSuite {

  val defaultPath = List("key2", "key1")
  val defaultPathStr = "key1.key2"

  def conf(confStr: String): ConfigValue = {
    ConfigFactory.parseString(s"aux = $confStr").getValue("aux")
  }

  def cursor(confStr: String, pathElems: List[String] = defaultPath): ConfigCursor =
    ConfigCursor(conf(confStr), pathElems)

  behavior of "ConfigCursor"

  it should "provide a correct path string" in {
    cursor("abc").path shouldBe defaultPathStr
    cursor("abc", Nil).path shouldBe ""
  }

  it should "allow being casted to string in a safe way" in {
    cursor("abc").asString shouldBe
      Right("abc")

    cursor("[1, 2]").asString should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.STRING), None, defaultPathStr))

    cursor("{ a: 1, b: 2 }").asString should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING), None, defaultPathStr))
  }

  it should "allow being casted to a list cursor in a safe way" in {
    cursor("abc").asListCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST), None, defaultPathStr))

    cursor("[1, 2]").asListCursor shouldBe
      Right(ConfigListCursor(conf("[1, 2]").asInstanceOf[ConfigList], defaultPath))

    cursor("{ a: 1, b: 2 }").asListCursor should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST), None, defaultPathStr))
  }

  it should "allow being casted to a list of cursors in a safe way" in {
    cursor("abc").asList should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST), None, defaultPathStr))

    cursor("[1, 2]").asList shouldBe
      Right(List(cursor("1", "0" :: defaultPath), cursor("2", "1" :: defaultPath)))

    cursor("{ a: 1, b: 2 }").asList should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST), None, defaultPathStr))
  }

  it should "allow being casted to an object cursor in a safe way" in {
    cursor("abc").asObjectCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT), None, defaultPathStr))

    cursor("[1, 2]").asObjectCursor should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT), None, defaultPathStr))

    cursor("{ a: 1, b: 2 }").asObjectCursor shouldBe
      Right(ConfigObjectCursor(conf("{ a: 1, b: 2 }").asInstanceOf[ConfigObject], defaultPath))
  }

  it should "allow being casted to a map of cursors in a safe way" in {
    cursor("abc").asMap should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT), None, defaultPathStr))

    cursor("[1, 2]").asMap should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT), None, defaultPathStr))

    cursor("{ a: 1, b: 2 }").asMap shouldBe
      Right(Map("a" -> cursor("1", "a" :: defaultPath), "b" -> cursor("2", "b" :: defaultPath)))
  }

  it should "allow being casted to a collection cursor in a safe way" in {
    cursor("abc").asCollectionCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST, ConfigValueType.OBJECT), None, defaultPathStr))

    cursor("[1, 2]").asCollectionCursor shouldBe
      Right(Left(ConfigListCursor(conf("[1, 2]").asInstanceOf[ConfigList], defaultPath)))

    cursor("{ a: 1, b: 2 }").asCollectionCursor shouldBe
      Right(Right(ConfigObjectCursor(conf("{ a: 1, b: 2 }").asInstanceOf[ConfigObject], defaultPath)))
  }

  it should "handle in a safe way cursors to undefined values" in {
    val cur = ConfigCursor(null, defaultPath)
    cur.path shouldBe defaultPathStr
    cur.isUndefined shouldBe true
    cur.isNull shouldBe false
    cur.asString should failWithType[KeyNotFound]
    cur.asListCursor should failWithType[KeyNotFound]
    cur.asList should failWithType[KeyNotFound]
    cur.asObjectCursor should failWithType[KeyNotFound]
    cur.asMap should failWithType[KeyNotFound]
    cur.asCollectionCursor should failWithType[KeyNotFound]
  }

  behavior of "ConfigListCursor"

  def listCursor(confStr: String, pathElems: List[String] = defaultPath): ConfigListCursor =
    cursor(confStr, pathElems).asListCursor.right.get

  it should "have correct isEmpty and size methods" in {
    listCursor("[1, 2]").isEmpty shouldBe false
    listCursor("[]").isEmpty shouldBe true
    listCursor("[1, 2]").size shouldBe 2
    listCursor("[]").size shouldBe 0
  }

  it should "allow access to a given index in a safe way" in {
    listCursor("[1, 2]").atIndex(0) shouldBe Right(cursor("1", "0" :: defaultPath))
    listCursor("[1, 2]").atIndex(1) shouldBe Right(cursor("2", "1" :: defaultPath))
    listCursor("[1, 2]").atIndex(2) should failWith(KeyNotFound(s"$defaultPathStr.2", None, Set()))
  }

  it should "allow access to a given index returning an undefined value cursor on out-of-range indices" in {
    listCursor("[1, 2]").atIndexOrUndefined(0) shouldBe cursor("1", "0" :: defaultPath)
    listCursor("[1, 2]").atIndexOrUndefined(1) shouldBe cursor("2", "1" :: defaultPath)
    listCursor("[1, 2]").atIndexOrUndefined(2).isUndefined shouldBe true
  }

  it should "provide a tailOption method that keeps the absolute paths correct" in {
    listCursor("[1, 2]").tailOption shouldBe Some(listCursor("[2]").copy(offset = 1))
    listCursor("[1, 2]").tailOption.get.atIndex(0) shouldBe Right(cursor("2", "1" :: defaultPath))
    listCursor("[1, 2]").tailOption.get.atIndex(1) should failWith(KeyNotFound(s"$defaultPathStr.2", None, Set()))
    listCursor("[]").tailOption shouldBe None
  }

  it should "provide a direct conversion to a list of cursors" in {
    listCursor("[1, 2]").list shouldBe List(cursor("1", "0" :: defaultPath), cursor("2", "1" :: defaultPath))
    listCursor("[]").list shouldBe Nil
  }

  behavior of "ConfigObjectCursor"

  def objCursor(confStr: String, pathElems: List[String] = defaultPath): ConfigObjectCursor =
    cursor(confStr, pathElems).asObjectCursor.right.get

  it should "have correct isEmpty and size methods" in {
    objCursor("{ a: 1, b: 2 }").isEmpty shouldBe false
    objCursor("{}").isEmpty shouldBe true
    objCursor("{ a: 1, b: 2 }").size shouldBe 2
    objCursor("{}").size shouldBe 0
  }

  it should "provide the list of keys in the object" in {
    objCursor("{ a: 1, b: 2 }").keys.toSet shouldBe Set("a", "b")
    objCursor("{}").keys.toSet shouldBe Set.empty
  }

  it should "allow access to a given key in a safe way" in {
    objCursor("{ a: 1, b: 2 }").atKey("a") shouldBe Right(cursor("1", "a" :: defaultPath))
    objCursor("{ a: 1, b: 2 }").atKey("c") should failWith(KeyNotFound(s"$defaultPathStr.c", None, Set()))
  }

  it should "allow access to a given key returning an undefined value cursor on non-existing keys" in {
    objCursor("{ a: 1, b: 2 }").atKeyOrUndefined("a") shouldBe cursor("1", "a" :: defaultPath)
    objCursor("{ a: 1, b: 2 }").atKeyOrUndefined("c").isUndefined shouldBe true
  }

  it should "provide a correct withoutKey method" in {
    objCursor("{ a: 1, b: 2 }").withoutKey("a") shouldBe objCursor("{ b: 2 }")
    objCursor("{ a: 1, b: 2 }").withoutKey("c") shouldBe objCursor("{ a: 1, b: 2 }")
  }

  it should "provide a direct conversion to a map of cursors" in {
    objCursor("{ a: 1, b: 2 }").map shouldBe Map("a" -> cursor("1", "a" :: defaultPath), "b" -> cursor("2", "b" :: defaultPath))
    objCursor("{}").map shouldBe Map.empty
  }
}
