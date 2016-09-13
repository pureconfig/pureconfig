package pureconfig

class WordDelimiterConverter[T](typeFieldDelimiter: WordDelimiter, configFieldDelimiter: WordDelimiter) {
  def fromTypeToConfigField(s: String) =
    (typeFieldDelimiter.toTokens _ andThen configFieldDelimiter.fromTokens _)(s)
}

object WordDelimiterConverter extends LowPriorityWordDelimiterConvertImplicits {
  def apply[T](implicit conv: WordDelimiterConverter[T]): WordDelimiterConverter[T] = conv
}

trait LowPriorityWordDelimiterConvertImplicits {
  implicit def wordDelimiterConverter[T] =
    new WordDelimiterConverter[T](NoWordDelimiter, NoWordDelimiter)
}
