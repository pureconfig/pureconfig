package pureconfig.module.cats

import cats._
import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures }

package object instances {

  implicit val configReaderInstance: ApplicativeError[ConfigReader, ConfigReaderFailures] =
    new ApplicativeError[ConfigReader, ConfigReaderFailures] {
      def pure[A](x: A): ConfigReader[A] =
        ConfigReader.fromFunction { _ => Right(x) }

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

  implicit val configWriterCatsInstance: ContravariantSemigroupal[ConfigWriter] =
    new ContravariantSemigroupal[ConfigWriter] {
      def contramap[A, B](fa: ConfigWriter[A])(f: B => A): ConfigWriter[B] =
        fa.contramap(f)

      def product[A, B](fa: ConfigWriter[A], fb: ConfigWriter[B]) =
        ConfigWriter.fromFunction[(A, B)] {
          case (a, b) =>
            fb.to(b).withFallback(fa.to(a))
        }
    }

  implicit val configConvertCatsInstance: InvariantSemigroupal[ConfigConvert] =
    new InvariantSemigroupal[ConfigConvert] {
      def imap[A, B](fa: ConfigConvert[A])(f: A => B)(g: B => A): ConfigConvert[B] =
        fa.xmap(f, g)

      def product[A, B](fa: ConfigConvert[A], fb: ConfigConvert[B]): ConfigConvert[(A, B)] = {
        val reader = fa.zip(fb)
        val writer = ConfigWriter.fromFunction[(A, B)] {
          case (a, b) =>
            fb.to(b).withFallback(fa.to(a))
        }

        ConfigConvert.fromReaderAndWriter(
          Derivation.Successful(reader),
          Derivation.Successful(writer))
      }
    }

  implicit val configValueEq: Eq[ConfigValue] = Eq.fromUniversalEquals
  implicit val configReaderFailureEq: Eq[ConfigReaderFailure] = Eq.fromUniversalEquals
  implicit val configReaderFailuresEq: Eq[ConfigReaderFailures] = Eq.fromUniversalEquals

  implicit val configReaderFailuresSemigroup: Semigroup[ConfigReaderFailures] =
    Semigroup.instance(_ ++ _)

  implicit val configValueCatsSemigroup: Semigroup[ConfigValue] =
    Semigroup.instance((a, b) => b.withFallback(a))
}
