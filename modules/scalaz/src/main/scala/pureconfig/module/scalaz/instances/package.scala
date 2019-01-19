package pureconfig.module.scalaz

import com.typesafe.config.ConfigValue
import pureconfig.{ ConfigConvert, ConfigReader, ConfigWriter }
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures, FailureReason }

import scalaz.{ Contravariant, Equal, InvariantFunctor, MonadError, Semigroup, Show }

/**
 * Instances of `scalaz` type classes for `ConfigReader`, `ConfigWriter`, `ConfigConvert`
 * and other `pureconfig` citizens.
 */
package object instances {

  implicit val configReaderInstance: MonadError[ConfigReader, ConfigReaderFailures] =
    new MonadError[ConfigReader, ConfigReaderFailures] {
      def point[A](a: => A): ConfigReader[A] =
        ConfigReader.fromFunction { _ => Right(a) }

      def bind[A, B](fa: ConfigReader[A])(f: A => ConfigReader[B]): ConfigReader[B] =
        fa.flatMap(f)

      def raiseError[A](e: ConfigReaderFailures): ConfigReader[A] =
        ConfigReader.fromFunction { _ => Left(e) }

      def handleError[A](fa: ConfigReader[A])(f: ConfigReaderFailures => ConfigReader[A]): ConfigReader[A] =
        ConfigReader.fromFunction { cv =>
          fa.from(cv) match {
            case Left(failures) => f(failures).from(cv)
            case r @ Right(_) => r
          }
        }
    }

  implicit val configWriterInstance: Contravariant[ConfigWriter] =
    new Contravariant[ConfigWriter] {
      def contramap[A, B](fa: ConfigWriter[A])(f: B => A): ConfigWriter[B] =
        fa.contramap(f)
    }

  implicit val configConvertInstance: InvariantFunctor[ConfigConvert] =
    new InvariantFunctor[ConfigConvert] {
      def xmap[A, B](ma: ConfigConvert[A], f: A => B, g: B => A): ConfigConvert[B] =
        ma.xmap(f, g)
    }

  implicit val configValueEqual: Equal[ConfigValue] = Equal.equalA
  implicit val failureReasonEqual: Equal[FailureReason] = Equal.equalA
  implicit val configReaderFailureEqual: Equal[ConfigReaderFailure] = Equal.equalA
  implicit val configReaderFailuresEqual: Equal[ConfigReaderFailures] = Equal.equalA

  implicit val failureReasonShow: Show[FailureReason] = Show.showFromToString
  implicit val configReaderFailureShow: Show[ConfigReaderFailure] = Show.showFromToString
  implicit val configReaderFailuresShow: Show[ConfigReaderFailures] = Show.showFromToString

  implicit val configReaderFailuresSemigroup: Semigroup[ConfigReaderFailures] =
    Semigroup.instance(_ ++ _)

  implicit val configValueSemigroup: Semigroup[ConfigValue] =
    Semigroup.instance((a, b) => b.withFallback(a))
}
