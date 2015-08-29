package pureconfig

/**
 * @author Mario Pastorelli
 */

object Main {

  // demo

  // product and nested
  case class EmailConfig(host: String, port: Int, msg: String)
  case class GenericConfig(email: EmailConfig, numTests: Int)

  // coproduct and nested
  sealed trait Parent
  case class Foo(number: Int) extends Parent
  case class Bar(name: String) extends Parent
  case class Test(parent: Parent)

  case class OptionTest(maybe: Option[Int], required: Long)

  def main(args: Array[String]): Unit = {
    println("EmailConfig: " + loadConfig[EmailConfig](Map("host" -> "foobar", "port" -> "10101", "msg" -> "lol")))
    println("GenericConfig: " + loadConfig[GenericConfig](Map("email.host" -> "foobar", "email.port" -> "10101", "email.msg" -> "lol", "numTests" -> "0")))

    println("Foo: " + loadConfig[Foo](Map("number" -> "10")))
    println("Bar: " + loadConfig[Bar](Map("name" -> "Mario")))
    println("Parent: " + loadConfig[Parent](Map("name" -> "Mario")))
    println("Parent: " + loadConfig[Parent](Map("number" -> "10")))
    println("Test: " + loadConfig[Test](Map("parent.name" -> "Mario")))
    println("Test: " + loadConfig[Test](Map("parent.number" -> "10")))

    println("OptionTest: " + loadConfig[OptionTest](Map("maybe" -> "1", "required" -> "3")))
    println("OptionTest: " + loadConfig[OptionTest](Map("required" -> "3")))

    println("Test with typesafe: " + loadConfig[GenericConfig])
  }
}
