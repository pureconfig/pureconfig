package pureconfig

import java.net.URL

import com.typesafe.config.{ConfigOrigin, ConfigOriginFactory}

import scala.reflect.ClassTag
import org.scalatest._
import org.scalatest.matchers.{MatchResult, Matcher}
import pureconfig.error._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait ConfigReaderMatchers { this: AnyFlatSpec with Matchers =>

  def failWith(reason: FailureReason): Matcher[ConfigReader.Result[Any]] =
    matchPattern { case Left(ConfigReaderFailures(ConvertFailure(`reason`, _, _))) => }

  def failWith(
      reason: FailureReason,
      path: String,
      origin: Option[ConfigOrigin] = None
  ): Matcher[ConfigReader.Result[Any]] =
    be(Left(ConfigReaderFailures(ConvertFailure(reason, origin, path))))

  def failWith(failure: ConfigReaderFailure): Matcher[ConfigReader.Result[Any]] =
    be(Left(ConfigReaderFailures(failure)))

  def failWithConvertFailureOf[Reason <: FailureReason: ClassTag]: Matcher[ConfigReader.Result[Any]] =
    matchPattern { case Left(ConfigReaderFailures(ConvertFailure(_: Reason, _, _))) => }

  def failWithType[Failure <: ConfigReaderFailure: ClassTag]: Matcher[ConfigReader.Result[Any]] =
    matchPattern { case Left(ConfigReaderFailures(_: Failure)) => }

  def failLike(pf: PartialFunction[ConfigReaderFailure, MatchResult]) =
    new Matcher[ConfigReader.Result[Any]] with Inside with PartialFunctionValues {

      def apply(left: ConfigReader.Result[Any]): MatchResult = {
        inside(left) { case Left(ConfigReaderFailures(failure)) => pf.valueAt(failure) }
      }
    }

  def stringConfigOrigin(line: Int) =
    Some(ConfigOriginFactory.newSimple("String").withLineNumber(line))

  def urlConfigOrigin(url: URL, line: Int): Option[ConfigOrigin] =
    Some(ConfigOriginFactory.newURL(url).withLineNumber(line))

  val emptyConfigOrigin: Option[ConfigOrigin] =
    Some(ConfigOriginFactory.newSimple())
}
