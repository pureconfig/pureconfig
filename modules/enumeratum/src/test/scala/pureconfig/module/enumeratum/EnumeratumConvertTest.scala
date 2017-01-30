package pureconfig.module.enumeratum

import com.typesafe.config.ConfigFactory
import enumeratum.EnumEntry.{ Snakecase, Uppercase }
import enumeratum._
import enumeratum.values._
import org.scalatest.{ FlatSpec, Matchers, TryValues }
import pureconfig.syntax._
import org.scalatest.Inspectors._

class EnumeratumConvertTest extends FlatSpec with Matchers with TryValues {
  sealed trait Greeting extends EnumEntry with Snakecase

  object Greeting extends Enum[Greeting] {
    val values = findValues
    case object Hello extends Greeting
    case object GoodBye extends Greeting
    case object ShoutGoodBye extends Greeting with Uppercase
  }

  "Enumeratum ConfigConvert" should "parse an enum" in forAll(Greeting.values) {
    greeting =>
      val conf = ConfigFactory.parseString(s"""{greeting:"${greeting.entryName}"}""")
      case class Conf(greeting: Greeting)
      conf.to[Conf].success.value shouldEqual Conf(greeting)
  }

  sealed abstract class IntLibraryItem(val value: Int, val name: String) extends IntEnumEntry

  case object IntLibraryItem extends IntEnum[IntLibraryItem] {
    val values = findValues
    case object Book extends IntLibraryItem(value = 1, name = "book")
    case object Movie extends IntLibraryItem(name = "movie", value = 2)
    case object Magazine extends IntLibraryItem(3, "magazine")
    case object CD extends IntLibraryItem(4, name = "cd")
  }

  it should "parse an int enum" in forAll(IntLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: IntLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }

  sealed abstract class LongLibraryItem(val value: Long, val name: String) extends LongEnumEntry

  case object LongLibraryItem extends LongEnum[LongLibraryItem] {
    val values = findValues
    case object Book extends LongLibraryItem(value = 1L, name = "book")
    case object Movie extends LongLibraryItem(name = "movie", value = 2L)
    case object Magazine extends LongLibraryItem(3L, "magazine")
    case object CD extends LongLibraryItem(4L, name = "cd")
  }

  it should "parse a long value enum" in forAll(LongLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: LongLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }

  sealed abstract class ShortLibraryItem(val value: Short, val name: String) extends ShortEnumEntry

  case object ShortLibraryItem extends ShortEnum[ShortLibraryItem] {
    val values = findValues
    case object Book extends ShortLibraryItem(value = 1, name = "book")
    case object Movie extends ShortLibraryItem(name = "movie", value = 2)
    case object Magazine extends ShortLibraryItem(3, "magazine")
    case object CD extends ShortLibraryItem(4, name = "cd")
  }

  it should "parse a short value enum" in forAll(ShortLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: ShortLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }

  sealed abstract class StringLibraryItem(val value: String, val number: Int) extends StringEnumEntry

  case object StringLibraryItem extends StringEnum[StringLibraryItem] {
    val values = findValues
    case object Book extends StringLibraryItem(number = 1, value = "book")
    case object Movie extends StringLibraryItem(value = "movie", number = 2)
    case object Magazine extends StringLibraryItem("magazine", 3)
    case object CD extends StringLibraryItem("cd", number = 4)
  }

  it should "parse a string value enum" in forAll(StringLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: StringLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }

  sealed abstract class ByteLibraryItem(val value: Byte, val name: String) extends ByteEnumEntry

  case object ByteLibraryItem extends ByteEnum[ByteLibraryItem] {
    val values = findValues
    case object Book extends ByteLibraryItem(value = 1, name = "book")
    case object Movie extends ByteLibraryItem(name = "movie", value = 2)
    case object Magazine extends ByteLibraryItem(3, "magazine")
    case object CD extends ByteLibraryItem(4, name = "cd")
  }

  it should "parse a byte value enum" in forAll(ByteLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: ByteLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }

  sealed abstract class CharLibraryItem(val value: Char, val number: Int) extends CharEnumEntry

  case object CharLibraryItem extends CharEnum[CharLibraryItem] {
    val values = findValues
    case object Book extends CharLibraryItem(number = 1, value = 'a')
    case object Movie extends CharLibraryItem(value = 'b', number = 2)
    case object Magazine extends CharLibraryItem('c', 3)
    case object CD extends CharLibraryItem('d', number = 4)
  }

  it should "parse a char value enum" in forAll(CharLibraryItem.values) {
    item =>
      val conf = ConfigFactory.parseString(s"""{item:"${item.value}"}""")
      case class Conf(item: CharLibraryItem)
      conf.to[Conf].success.value shouldEqual Conf(item)
  }
}
