package pureconfig

trait NamingConvention {
  def toTokens(s: String): Seq[String]
  def fromTokens(l: Seq[String]): String
}

trait CapitalizedWordsNamingConvention extends NamingConvention {
  def toTokens(s: String): Seq[String] = {
    if (s.isEmpty) List.empty
    else {
      val buffer = List.newBuilder[String]
      val currentTokenBuilder = new StringBuilder
      val noneCtx = 0
      val lowerCaseCtx = 1
      val upperCaseCtx = 2
      val notLetterCtx = 3
      var ctx = noneCtx
      val _ = {
        val c = s.charAt(0)
        if ('a' <= c && c <= 'z') {
          currentTokenBuilder.append(c)
          ctx = lowerCaseCtx
        } else if ('A' <= c && c <= 'Z') {
          currentTokenBuilder.append(c.toLower)
          ctx = upperCaseCtx
        } else {
          currentTokenBuilder.append(c)
          ctx = notLetterCtx
        }
      }
      var i = 1
      while (i < s.length) {
        val c = s.charAt(i)
        if ('a' <= c && c <= 'z') {
          if (ctx == upperCaseCtx && currentTokenBuilder.length > 1) {
            val previousChar = currentTokenBuilder.charAt(currentTokenBuilder.length - 1)
            currentTokenBuilder.deleteCharAt(currentTokenBuilder.length - 1)
            buffer += currentTokenBuilder.result()
            currentTokenBuilder.clear()
            currentTokenBuilder.append(previousChar)
          }
          currentTokenBuilder.append(c)
          ctx = lowerCaseCtx
        } else if ('A' <= c && c <= 'Z') {
          if (ctx != upperCaseCtx) {
            buffer += currentTokenBuilder.result()
            currentTokenBuilder.clear()
          }
          currentTokenBuilder.append(c.toLower)
          ctx = upperCaseCtx
        } else {
          if (ctx != notLetterCtx) {
            buffer += currentTokenBuilder.result()
            currentTokenBuilder.clear()
          }
          currentTokenBuilder.append(c)
          ctx = notLetterCtx
        }
        i += 1
      }
      buffer += currentTokenBuilder.result()
      buffer.result()
    }
  }
}

/** CamelCase identifiers look like `camelCase` and `useMorePureconfig`
  * @see
  *   https://en.wikipedia.org/wiki/Camel_case
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

/** PascalCase identifiers look like e.g.`PascalCase` and `UseMorePureconfig`
  * @see
  *   https://en.wikipedia.org/wiki/PascalCase
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

/** KebabCase identifiers look like `kebab-case` and `use-more-pureconfig`
  * @see
  *   http://wiki.c2.com/?KebabCase
  */
object KebabCase extends StringDelimitedNamingConvention("-")

/** SnakeCase identifiers look like `snake_case` and `use_more_pureconfig`
  * @see
  *   https://en.wikipedia.org/wiki/Snake_case
  */
object SnakeCase extends StringDelimitedNamingConvention("_")

/** SnakeCase identifiers look like `SCREAMING_SNAKE_CASE` and `USE_MORE_PURECONFIG`
  * @see
  *   https://en.wikipedia.org/wiki/Snake_case (and search for SCREAMING_SNAKE_CASE)
  */
object ScreamingSnakeCase extends NamingConvention {
  def toTokens(s: String): Seq[String] = SnakeCase.toTokens(s)
  def fromTokens(l: Seq[String]): String = l.map(_.toUpperCase).mkString("_")
}
