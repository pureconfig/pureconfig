package pureconfig.module.scalaz

import com.typesafe.config.ConfigValue
import pureconfig.{ConfigConvert, ConfigReader}
import pureconfig.error.{ConfigReaderFailure, ConfigReaderFailures, FailureReason}

import scalaz.{\/, Maybe, NonEmptyList, Validation}

import scala.reflect.ClassTag

/**
  * Useful extension methods that bring `scalaz` data structures into `pureconfig` world.
  */
package object syntax {

  implicit final class ConfigConvertCompanionObjectOps(val co: ConfigConvert.type) extends AnyVal {
    def viaStringDisjunction[A](fromF: String => FailureReason \/ A, toF: A => String): ConfigConvert[A] =
      co.viaString(fromF.andThen(_.toEither), toF)

    def viaStringMaybe[A: ClassTag](fromF: String => Maybe[A], toF: A => String): ConfigConvert[A] =
      co.viaStringOpt[A](fromF.andThen(_.toOption), toF)

    def viaNonEmptyStringDisjunction[A: ClassTag](
        fromF: String => FailureReason \/ A,
        toF: A => String
    ): ConfigConvert[A] =
      co.viaNonEmptyString(fromF.andThen(_.toEither), toF)

    def viaNonEmptyStringMaybe[A: ClassTag](fromF: String => Maybe[A], toF: A => String): ConfigConvert[A] =
      co.viaNonEmptyStringOpt(fromF.andThen(_.toOption), toF)
  }

  implicit final class ConfigReaderFailuresOps(val failures: ConfigReaderFailures) extends AnyVal {
    def toNel: NonEmptyList[ConfigReaderFailure] = NonEmptyList.fromSeq(failures.head, failures.tail)
  }

  implicit final class ConfigReaderCompanionObjectOps(val co: ConfigReader.type) extends AnyVal {
    def fromFunctionDisjunction[A](fromF: ConfigValue => ConfigReaderFailures \/ A): ConfigReader[A] =
      co.fromFunction(fromF.andThen(_.toEither))

    def fromFunctionValidation[A](fromF: ConfigValue => Validation[ConfigReaderFailures, A]): ConfigReader[A] =
      co.fromFunction(fromF.andThen(_.toEither))

    def fromStringDisjunction[A](fromF: String => FailureReason \/ A): ConfigReader[A] =
      co.fromString(fromF.andThen(_.toEither))

    def fromStringMaybe[A: ClassTag](fromF: String => Maybe[A]): ConfigReader[A] =
      co.fromStringOpt[A](fromF.andThen(_.toOption))

    def fromNonEmptyStringDisjunction[A: ClassTag](fromF: String => FailureReason \/ A): ConfigReader[A] =
      co.fromNonEmptyString(fromF.andThen(_.toEither))

    def fromNonEmptyStringMaybe[A: ClassTag](fromF: String => Maybe[A]): ConfigReader[A] =
      co.fromNonEmptyStringOpt[A](fromF.andThen(_.toOption))
  }
}
