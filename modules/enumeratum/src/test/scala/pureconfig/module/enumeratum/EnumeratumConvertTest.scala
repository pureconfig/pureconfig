package pureconfig.module.enumeratum

import com.typesafe.config.ConfigFactory
import enumeratum.EnumEntry.{Snakecase, Uppercase}
import enumeratum._
import enumeratum.values._
import org.scalatest.Inspectors

import pureconfig.BaseSuite
import pureconfig.error.CannotConvert
import pureconfig.syntax._

class EnumeratumConvertTest extends BaseSuite {
  sealed trait Greeting extends EnumEntry with Snakecase

  object Greeting extends Enum[Greeting] {
    val values = findValues
    case object Hello extends Greeting
    case object GoodBye extends Greeting
    case object ShoutGoodBye extends Greeting with Uppercase
  }

  "Enumeratum ConfigConvert" should "parse an enum" in Inspectors.forAll(Greeting.values) { greeting =>
    configValue(s""""${greeting.entryName}"""").to[Greeting].value shouldEqual greeting
  }

  sealed abstract class IntLibraryItem(val value: Int, val name: String) extends IntEnumEntry

  case object IntLibraryItem extends IntEnum[IntLibraryItem] {
    val values = findValues
    case object Book extends IntLibraryItem(value = 1, name = "book")
    case object Movie extends IntLibraryItem(name = "movie", value = 2)
    case object Magazine extends IntLibraryItem(3, "magazine")
    case object CD extends IntLibraryItem(4, name = "cd")
  }

  it should "parse an int enum" in Inspectors.forAll(IntLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[IntLibraryItem].value shouldEqual item
  }

  sealed abstract class LongLibraryItem(val value: Long, val name: String) extends LongEnumEntry

  case object LongLibraryItem extends LongEnum[LongLibraryItem] {
    val values = findValues
    case object Book extends LongLibraryItem(value = 1L, name = "book")
    case object Movie extends LongLibraryItem(name = "movie", value = 2L)
    case object Magazine extends LongLibraryItem(3L, "magazine")
    case object CD extends LongLibraryItem(4L, name = "cd")
  }

  it should "parse a long value enum" in Inspectors.forAll(LongLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[LongLibraryItem].value shouldEqual item
  }

  sealed abstract class ShortLibraryItem(val value: Short, val name: String) extends ShortEnumEntry

  case object ShortLibraryItem extends ShortEnum[ShortLibraryItem] {
    val values = findValues
    case object Book extends ShortLibraryItem(value = 1, name = "book")
    case object Movie extends ShortLibraryItem(name = "movie", value = 2)
    case object Magazine extends ShortLibraryItem(3, "magazine")
    case object CD extends ShortLibraryItem(4, name = "cd")
  }

  it should "parse a short value enum" in Inspectors.forAll(ShortLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[ShortLibraryItem].value shouldEqual item
  }

  sealed abstract class StringLibraryItem(val value: String, val number: Int) extends StringEnumEntry

  case object StringLibraryItem extends StringEnum[StringLibraryItem] {
    val values = findValues
    case object Book extends StringLibraryItem(number = 1, value = "book")
    case object Movie extends StringLibraryItem(value = "movie", number = 2)
    case object Magazine extends StringLibraryItem("magazine", 3)
    case object CD extends StringLibraryItem("cd", number = 4)
    case object Empty extends StringLibraryItem("", number = 5)
  }

  it should "parse a string value enum" in Inspectors.forAll(StringLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[StringLibraryItem].value shouldEqual item
  }

  sealed abstract class ByteLibraryItem(val value: Byte, val name: String) extends ByteEnumEntry

  case object ByteLibraryItem extends ByteEnum[ByteLibraryItem] {
    val values = findValues
    case object Book extends ByteLibraryItem(value = 1, name = "book")
    case object Movie extends ByteLibraryItem(name = "movie", value = 2)
    case object Magazine extends ByteLibraryItem(3, "magazine")
    case object CD extends ByteLibraryItem(4, name = "cd")
  }

  it should "parse a byte value enum" in Inspectors.forAll(ByteLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[ByteLibraryItem].value shouldEqual item
  }

  sealed abstract class CharLibraryItem(val value: Char, val number: Int) extends CharEnumEntry

  case object CharLibraryItem extends CharEnum[CharLibraryItem] {
    val values = findValues
    case object Book extends CharLibraryItem(number = 1, value = 'a')
    case object Movie extends CharLibraryItem(value = 'b', number = 2)
    case object Magazine extends CharLibraryItem('c', 3)
    case object CD extends CharLibraryItem('d', number = 4)
  }

  it should "parse a char value enum" in Inspectors.forAll(CharLibraryItem.values) { item =>
    configValue(s""""${item.value}"""").to[CharLibraryItem].value shouldEqual item
  }

  it should "not parse a char value enum when given a string with more than one character" in {
    configValue(s""""string"""").to[CharLibraryItem] should failWithReason[CannotConvert]
  }
}
