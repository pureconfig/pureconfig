package pureconfig.module.cats

import cats._
import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures }

package object instances {

  implicit val configReaderInstance: ApplicativeError[ConfigReader, ConfigReaderFailures] =
    new ApplicativeError[ConfigReader, ConfigReaderFailures] {
      def pure[A](x: A): ConfigReader[A] =
        ConfigReader(ConfigReader.fromFunction { _ => Right(x) })

      def ap[A, B](ff: ConfigReader[A => B])(fa: ConfigReader[A]): ConfigReader[B] =
        ff.zip(fa).map { case (f, a) => f(a) }

      def raiseError[A](e: ConfigReaderFailures): ConfigReader[A] =
        ConfigReader.fromFunction { _ => Left(e) }

      def handleErrorWith[A](fa: ConfigReader[A])(f: ConfigReaderFailures => ConfigReader[A]): ConfigReader[A] =
        ConfigReader.fromFunction { cv =>
          fa.from(cv) match {
            case Left(failures) => f(failures).from(cv)
            case r @ Right(_) => r
          }
        }
    }

  implicit val configWriterCatsInstance: Contravariant[ConfigWriter] = new Contravariant[ConfigWriter] {
    def contramap[A, B](fa: ConfigWriter[A])(f: B => A): ConfigWriter[B] =
      fa.contramap(f)
  }

  implicit val configConvertCatsInstance: Invariant[ConfigConvert] = new Invariant[ConfigConvert] {
    def imap[A, B](fa: ConfigConvert[A])(f: A => B)(g: B => A): ConfigConvert[B] =
      fa.xmap(f, g)
  }

  implicit val configValueEq: Eq[ConfigValue] = Eq.fromUniversalEquals
  implicit val configReaderFailureEq: Eq[ConfigReaderFailure] = Eq.fromUniversalEquals
  implicit val configReaderFailuresEq: Eq[ConfigReaderFailures] = Eq.fromUniversalEquals
}
