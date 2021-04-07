package pureconfig

import com.typesafe.config._

import pureconfig.error.{CannotConvert, KeyNotFound, WrongType}

class ConfigCursorSuite extends BaseSuite {

  val defaultPath = List("key2", "key1")
  val defaultPathStr = "key1.key2"

  def conf(confStr: String): ConfigValue = {
    ConfigFactory.parseString(s"aux = $confStr").root.get("aux")
  }

  def cursor(confStr: String, pathElems: List[String] = defaultPath): ConfigCursor =
    ConfigCursor(conf(confStr), pathElems)

  behavior of "ConfigCursor"

  it should "provide a correct path string" in {
    cursor("abc").path shouldBe defaultPathStr
    cursor("abc", Nil).path shouldBe ""
  }

  it should "allow being casted to string in a safe way" in {
    cursor("abc").asString shouldBe Right("abc")
    cursor("4").asString shouldBe Right("4")
    cursor("true").asString shouldBe Right("true")

    cursor("null").asString should failWith(
      WrongType(ConfigValueType.NULL, Set(ConfigValueType.STRING)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("[1, 2]").asString should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.STRING)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("{ a: 1, b: 2 }").asString should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      defaultPathStr,
      stringConfigOrigin(1)
    )
  }

  it should "allow being cast to boolean in a safe way" in {
    cursor("true").asBoolean shouldBe Right(true)
    cursor("false").asBoolean shouldBe Right(false)

    cursor("abc").asBoolean should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.BOOLEAN)))
    cursor("1").asBoolean should failWith(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.BOOLEAN)))
    cursor("TRUE").asBoolean should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.BOOLEAN)))
  }

  it should "allow being cast to long in a safe way" in {
    cursor("3").asLong shouldBe Right(3L)

    cursor("abc").asLong should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)))
    cursor("true").asLong should failWith(WrongType(ConfigValueType.BOOLEAN, Set(ConfigValueType.NUMBER)))
    cursor("1.1").asLong should failWith(CannotConvert("1.1", "Long", "Unable to convert Number to Long"))
  }

  it should "allow being cast to int in a safe way" in {
    cursor("3").asInt shouldBe Right(3)

    cursor("abc").asInt should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)))
    cursor("true").asInt should failWith(WrongType(ConfigValueType.BOOLEAN, Set(ConfigValueType.NUMBER)))
    cursor("1.1").asInt should failWith(CannotConvert("1.1", "Int", "Unable to convert Number to Int"))
  }

  it should "allow being cast to short in a safe way" in {
    cursor("3").asShort shouldBe Right(3)

    cursor("abc").asShort should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)))
    cursor("true").asShort should failWith(WrongType(ConfigValueType.BOOLEAN, Set(ConfigValueType.NUMBER)))
    cursor("1.1").asShort should failWith(CannotConvert("1.1", "Short", "Unable to convert Number to Short"))
  }

  it should "allow being cast to double in a safe way" in {
    cursor("3").asDouble shouldBe Right(3.0)
    cursor("3.1").asDouble shouldBe Right(3.1)
    cursor("21412415121234567L").asDouble should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER))
    )

    cursor("abc").asDouble should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)))
    cursor("true").asDouble should failWith(WrongType(ConfigValueType.BOOLEAN, Set(ConfigValueType.NUMBER)))
  }

  it should "allow being cast to float in a safe way" in {
    cursor("3").asFloat shouldBe Right(3.0)
    cursor("1.1").asFloat shouldBe Right(1.1f)

    cursor("abc").asFloat should failWith(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)))
    cursor("true").asFloat should failWith(WrongType(ConfigValueType.BOOLEAN, Set(ConfigValueType.NUMBER)))
  }

  it should "allow being casted to a list cursor in a safe way" in {
    cursor("abc").asListCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("[1, 2]").asListCursor shouldBe
      Right(ConfigListCursor(conf("[1, 2]").asInstanceOf[ConfigList], defaultPath))

    cursor("{ a: 1, b: 2 }").asListCursor should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("{ 0: a, 1: b }").asListCursor shouldBe
      Right(ConfigListCursor(conf("""["a", "b"]""").asInstanceOf[ConfigList], defaultPath))

    cursor("{ 10: a, 3: b }").asListCursor shouldBe
      Right(ConfigListCursor(conf("""["b", "a"]""").asInstanceOf[ConfigList], defaultPath))

    cursor("{ 1: a, c: b }").asListCursor shouldBe
      Right(ConfigListCursor(conf("""["a"]""").asInstanceOf[ConfigList], defaultPath))

    cursor("{}").asListCursor should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )
  }

  it should "allow being casted to a list of cursors in a safe way" in {
    cursor("abc").asList should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("[1, 2]").asList shouldBe
      Right(List(cursor("1", "0" :: defaultPath), cursor("2", "1" :: defaultPath)))

    cursor("{ a: 1, b: 2 }").asList should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("{ 3: a, 10: b }").asList shouldBe
      Right(List(cursor("a", "0" :: defaultPath), cursor("b", "1" :: defaultPath)))

    cursor("{ 1: a, c: b }").asList shouldBe
      Right(List(cursor("a", "0" :: defaultPath)))

    cursor("{}").asList should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)),
      defaultPathStr,
      stringConfigOrigin(1)
    )
  }

  it should "allow being casted to an object cursor in a safe way" in {
    cursor("abc").asObjectCursor should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("[1, 2]").asObjectCursor should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("{ a: 1, b: 2 }").asObjectCursor shouldBe
      Right(ConfigObjectCursor(conf("{ a: 1, b: 2 }").asInstanceOf[ConfigObject], defaultPath))
  }

  it should "allow being casted to a map of cursors in a safe way" in {
    cursor("abc").asMap should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("[1, 2]").asMap should failWith(
      WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT)),
      defaultPathStr,
      stringConfigOrigin(1)
    )

    cursor("{ a: 1, b: 2 }").asMap shouldBe
      Right(Map("a" -> cursor("1", "a" :: defaultPath), "b" -> cursor("2", "b" :: defaultPath)))
  }

  it should "handle in a safe way cursors to undefined values" in {
    val cur = ConfigCursor(None, defaultPath)
    cur.path shouldBe defaultPathStr
    cur.isUndefined shouldBe true
    cur.isNull shouldBe false
    cur.asString should failWithReason[KeyNotFound]
    cur.asListCursor should failWithReason[KeyNotFound]
    cur.asList should failWithReason[KeyNotFound]
    cur.asObjectCursor should failWithReason[KeyNotFound]
    cur.asMap should failWithReason[KeyNotFound]
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
    listCursor("[1, 2]").atIndex(2) should failWith(KeyNotFound("2", Set()), defaultPathStr, stringConfigOrigin(1))
  }

  it should "allow access to a given index returning an undefined value cursor on out-of-range indices" in {
    listCursor("[1, 2]").atIndexOrUndefined(0) shouldBe cursor("1", "0" :: defaultPath)
    listCursor("[1, 2]").atIndexOrUndefined(1) shouldBe cursor("2", "1" :: defaultPath)
    listCursor("[1, 2]").atIndexOrUndefined(2).isUndefined shouldBe true
  }

  it should "provide a tailOption method that keeps the absolute paths correct" in {
    listCursor("[1, 2]").tailOption shouldBe Some(listCursor("[2]").copy(offset = 1))
    listCursor("[1, 2]").tailOption.get.atIndex(0) shouldBe Right(cursor("2", "1" :: defaultPath))
    listCursor("[1, 2]").tailOption.get
      .atIndex(1) should failWith(KeyNotFound("2", Set()), defaultPathStr, stringConfigOrigin(1))
    listCursor("[]").tailOption shouldBe None
  }

  it should "provide a direct conversion to a list of cursors" in {
    listCursor("[1, 2]").list shouldBe List(cursor("1", "0" :: defaultPath), cursor("2", "1" :: defaultPath))
    listCursor("[]").list shouldBe Nil
  }

  it should "retain the correct offset after calling the asListCursor method" in {
    listCursor("[1, 2]").tailOption.map(_.asListCursor) shouldBe (Some(Right(listCursor("[2]").copy(offset = 1))))
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
    objCursor("{ a: 1, b: 2 }")
      .atKey("c") should failWith(KeyNotFound("c", Set()), defaultPathStr, stringConfigOrigin(1))
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
    objCursor("{ a: 1, b: 2 }").map shouldBe Map(
      "a" -> cursor("1", "a" :: defaultPath),
      "b" -> cursor("2", "b" :: defaultPath)
    )
    objCursor("{}").map shouldBe Map.empty
  }
}
