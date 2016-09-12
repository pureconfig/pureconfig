package pureconfig

trait WordDelimiter {
  def toTokens(s: String): Seq[String]
  def fromTokens(l: Seq[String]): String
}

object CamelCaseWordDelimiter extends WordDelimiter {
  def toTokens(s: String): Seq[String] = {
    s.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"),
      " ").split(" ").map(_.toLowerCase)
  }

  def fromTokens(l: Seq[String]): String = {
    l match {
      case Seq() => ""
      case h +: Seq() => h.toLowerCase
      case h +: t => h.toLowerCase + t.map(_.capitalize).mkString
    }
  }
}

class StringWordDelimiter(d: String) extends WordDelimiter {
  def toTokens(s: String): Seq[String] =
    s.split(d).map(_.toLowerCase)

  def fromTokens(l: Seq[String]): String =
    l.map(_.toLowerCase).mkString(d)
}

object HyphenWordDelimiter extends StringWordDelimiter("-")

object UnderscoreWordDelimiter extends StringWordDelimiter("_")
