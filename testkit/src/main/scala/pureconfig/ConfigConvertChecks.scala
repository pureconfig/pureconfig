package pureconfig

import scala.reflect.ClassTag

import com.typesafe.config.{ConfigRenderOptions, ConfigValue, ConfigValueFactory}
import org.scalacheck.Arbitrary
import org.scalactic.Equality
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import pureconfig.error.{ConfigReaderFailures, ConvertFailure, FailureReason}

/** Add utilities to a scalatest `FlatSpec` to test `ConfigConvert` instances
  */
trait ConfigConvertChecks { this: AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks with EitherValues =>

  /** For each value of type `A`, check that the value produced by converting to and then from `ConfigValue` is the same
    * of the original value
    *
    * Note that this method doesn't check all the values but only the values that can be created by `Arbitrary[A]` and
    * only the `ConfigValue` created by `ConfigConvert[A].to`. While `Arbitrary[A]` is usually comprehensive,
    * `ConfigConvert[A].from` could support different kind of values that `ConfigConvert[A].to` doesn't produce because,
    * for instance, multiple representation of `a: A` are possible. Use [[checkRead]] for those representations.
    */
  def checkArbitrary[A](implicit
      cc: ConfigConvert[A],
      arb: Arbitrary[A],
      tpe: TypeStringCompat[A],
      equality: Equality[A]
  ): Unit =
    it should s"read an arbitrary ${tpe.typeName}" in forAll { (a: A) =>
      cc.from(cc.to(a)).value shouldEqual a
    }

  /** A more generic version of [[checkArbitrary]] where the type which will be written as `ConfigValue` is different
    * from the type which will be read from that `ConfigValue`. The idea being is to test the reading part of a
    * `ConfigConvert` by providing another type for which it's easy to create `Arbitrary` instances and write the values
    * to a configuration.
    *
    * For instance, to test that `Double` can be read from percentages, like `"42 %"`, we can create a dummy
    * [[pureconfig.data.Percentage]] class which contains an integer from `0` to `100`, write that percentage to a
    * `ConfigValue` representing a `String` and then try to read the percentage from the `ConfigValue` via
    * `ConfigConvert[Double].from`. Creating an instance of `Arbitrary[Percentage]` is simple, same for
    * `ConfigConvert[Percentage]`.
    *
    * @param f
    *   a function used to convert a value of type `T2` to a value of type `T1`. The result of the conversion to and
    *   from a `ConfigValue` will be tested against the output of this function.
    * @param cr
    *   the `ConfigConvert` used to read a value from a `ConfigValue`. This is the instance that we want to test
    * @param cw
    *   the `ConfigConvert` used to write a value to a `ConfigValue`. This is the dummy instance used to test `cr`
    * @param arb
    *   the `Arbitrary` used to generate values to write a `ConfigValue` via `cw`
    */
  def checkArbitrary2[A, B](f: B => A)(implicit
      cr: ConfigConvert[A],
      cw: ConfigConvert[B],
      arb: Arbitrary[B],
      tpe1: TypeStringCompat[A],
      tpe2: TypeStringCompat[B],
      equality: Equality[A]
  ): Unit =
    it should s"read a ${tpe1.typeName} from an arbitrary ${tpe2.typeName}" in forAll { (b: B) =>
      cr.from(cw.to(b)).value shouldEqual f(b)
    }

  /** For each pair of value of type `A` and `ConfigValue`, check that `ConfigReader[A].from` successfully converts the
    * latter into to former. Useful to test specific values
    */
  def checkRead[A: Equality](
      reprsToValues: (ConfigValue, A)*
  )(implicit cr: ConfigReader[A], tpe: TypeStringCompat[A]): Unit =
    for ((repr, value) <- reprsToValues) {
      it should s"read the value $value of type ${tpe.typeName} from ${repr.render(ConfigRenderOptions.concise())}" in {
        cr.from(repr).value shouldEqual value
      }
    }

  /** Similar to [[checkRead]] but work on ConfigValues of type String */
  def checkReadString[A: ConfigReader: TypeStringCompat: Equality](strsToValues: (String, A)*): Unit =
    checkRead[A](strsToValues.map { case (s, a) => ConfigValueFactory.fromAnyRef(s) -> a }: _*)

  /** For each pair of value of type `A` and `ConfigValue`, check that `ConfigWriter[A].to` successfully converts the
    * former into the latter. Useful to test specific values
    */
  def checkWrite[A](
      valuesToReprs: (A, ConfigValue)*
  )(implicit cw: ConfigWriter[A], tpe: TypeStringCompat[A]): Unit =
    for ((value, repr) <- valuesToReprs) {
      it should s"write the value $value of type ${tpe.typeName} to ${repr.render(ConfigRenderOptions.concise())}" in {
        cw.to(value) shouldEqual repr
      }
    }

  /** Similar to [[checkWrite]] but work on ConfigValues of type String */
  def checkWriteString[A: ConfigWriter: TypeStringCompat](valuesToStrs: (A, String)*): Unit =
    checkWrite[A](valuesToStrs.map { case (a, s) => a -> ConfigValueFactory.fromAnyRef(s) }: _*)

  /** For each pair of value of type `A` and `ConfigValue`, check that `ConfigReader[A].from` successfully converts the
    * latter into to former and `ConfigWriter[A].to` successfully converts the former into the latter.
    */
  def checkReadWrite[A: ConfigReader: ConfigWriter: TypeStringCompat: Equality](
      reprsValues: (ConfigValue, A)*
  ): Unit = {
    checkRead[A](reprsValues: _*)
    checkWrite[A](reprsValues.map(_.swap): _*)
  }

  /** Similar to [[checkReadWrite]] but work on ConfigValues of type String */
  def checkReadWriteString[A: ConfigReader: ConfigWriter: TypeStringCompat: Equality](
      strsValues: (String, A)*
  ): Unit = {
    checkReadString[A](strsValues: _*)
    checkWriteString[A](strsValues.map(_.swap): _*)
  }

  /** Check that `cc` returns error of type `E` when trying to read each value passed with `values`
    *
    * @param values
    *   the values that should not be conver
    * @param cr
    *   the `ConfigConvert` to test
    */
  def checkFailure[A, E <: FailureReason](
      values: ConfigValue*
  )(implicit cr: ConfigReader[A], tpe: TypeStringCompat[A], eTag: ClassTag[E]): Unit =
    for (value <- values) {
      it should s"fail when it tries to read a value of type ${tpe.typeName} " +
        s"from ${value.render(ConfigRenderOptions.concise())}" in {
          val result = cr.from(value)
          result.left.value.toList should have size 1
          result.left.value.head should matchPattern { case ConvertFailure(_: E, _, _) => }
        }
    }

  /** For each pair of `ConfigValue` and `ConfigReaderFailures`, check that `cr` fails with the provided errors when
    * trying to read the provided `ConfigValue`.
    */
  def checkFailures[A](
      valuesToErrors: (ConfigValue, ConfigReaderFailures)*
  )(implicit cr: ConfigReader[A], tpe: TypeStringCompat[A]): Unit =
    for ((value, errors) <- valuesToErrors) {
      it should s"fail when it tries to read a value of type ${tpe.typeName} " +
        s"from ${value.render(ConfigRenderOptions.concise())}" in {
          cr.from(value).left.value.toList should contain theSameElementsAs errors.toList
        }
    }
}
