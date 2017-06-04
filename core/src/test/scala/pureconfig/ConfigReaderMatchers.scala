package pureconfig

import scala.reflect.ClassTag

import org.scalatest._
import org.scalatest.matchers.{ MatchResult, Matcher }
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures }

trait ConfigReaderMatchers { this: FlatSpec with Matchers =>

  def failWith(failure: ConfigReaderFailure): Matcher[Either[ConfigReaderFailures, Any]] =
    be(Left(ConfigReaderFailures(failure, Nil)))

  def failWithType[Failure <: ConfigReaderFailure: ClassTag]: Matcher[Either[ConfigReaderFailures, Any]] =
    matchPattern { case Left(ConfigReaderFailures(_: Failure, Nil)) => }

  def failLike(pf: PartialFunction[ConfigReaderFailure, MatchResult]) =
    new Matcher[Either[ConfigReaderFailures, Any]] with Inside with PartialFunctionValues {

      def apply(left: Either[ConfigReaderFailures, Any]): MatchResult = {
        inside(left) { case Left(ConfigReaderFailures(failure, Nil)) => pf.valueAt(failure) }
      }
    }
}
