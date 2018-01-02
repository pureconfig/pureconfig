package pureconfig.module.cats

import scala.annotation.tailrec
import cats.{ Contravariant, Eq, Invariant, MonadError }
import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures }

package object instances {

  implicit val configReaderCatsInstance: MonadError[ConfigReader, ConfigReaderFailures] = {
    new MonadError[ConfigReader, ConfigReaderFailures] {

      def pure[A](x: A): ConfigReader[A] =
        ConfigReader.fromFunction { _ => Right(x) }

      def flatMap[A, B](fa: ConfigReader[A])(f: A => ConfigReader[B]): ConfigReader[B] =
        fa.flatMap(f)

      def tailRecM[A, B](a: A)(f: A => ConfigReader[Either[A, B]]): ConfigReader[B] = ConfigReader.fromFunction { cv =>
        @tailrec
        def loop(currA: A): Either[ConfigReaderFailures, B] = f(currA).from(cv) match {
          case Left(failures) => Left(failures)
          case Right(Right(b)) => Right(b)
          case Right(Left(nextA)) => loop(nextA)
        }
        loop(a)
      }

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
