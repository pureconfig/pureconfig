package pureconfig

trait NamingConvention {
  def toTokens(s: String): Seq[String]
  def fromTokens(l: Seq[String]): String
}

trait CapitalizedWordsNamingConvention extends NamingConvention {
  def toTokens(s: String): Seq[String] = {
    CapitalizedWordsNamingConvention.wordBreakPattern.split(s).map(_.toLowerCase)
  }
}

object CapitalizedWordsNamingConvention {
  private val wordBreakPattern =
    String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])").r
}

/**
  * CamelCase identifiers look like `camelCase` and `useMorePureconfig`
  * @see https://en.wikipedia.org/wiki/Camel_case
  */
object CamelCase extends CapitalizedWordsNamingConvention {
  def fromTokens(l: Seq[String]): String = {
    l match {
      case Seq() => ""
      case h +: Seq() => h.toLowerCase
      case h +: t => h.toLowerCase + t.map(_.capitalize).mkString
    }
  }
}

/**
  * PascalCase identifiers look like e.g.`PascalCase` and `UseMorePureconfig`
  * @see https://en.wikipedia.org/wiki/PascalCase
  */
object PascalCase extends CapitalizedWordsNamingConvention {
  def fromTokens(l: Seq[String]): String = l.map(_.capitalize).mkString
}

class StringDelimitedNamingConvention(d: String) extends NamingConvention {
  def toTokens(s: String): Seq[String] =
    s.split(d).map(_.toLowerCase)

  def fromTokens(l: Seq[String]): String =
    l.map(_.toLowerCase).mkString(d)
}

/**
  * KebabCase identifiers look like `kebab-case` and `use-more-pureconfig`
  * @see http://wiki.c2.com/?KebabCase
  */
object KebabCase extends StringDelimitedNamingConvention("-")

/**
  * SnakeCase identifiers look like `snake_case` and `use_more_pureconfig`
  * @see https://en.wikipedia.org/wiki/Snake_case
  */
object SnakeCase extends StringDelimitedNamingConvention("_")
