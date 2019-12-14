package pureconfig.module.squants

import org.scalacheck.{ Arbitrary, Gen }
import squants.market._
import squants.{ Dimension, Quantity }

package object arbitrary {

  def quantityAbitrary[T <: Quantity[T]](dim: Dimension[T]): Arbitrary[T] = {
    Arbitrary(
      for {
        n <- Arbitrary.arbitrary[Double]
        u <- Gen.oneOf(dim.units.toList)
      } yield u(n))
  }

  // Money.units is not implemented so we need an explicit Arbitrary
  implicit val moneyArbitrary: Arbitrary[Money] = {

    // BTC is not included: fails on input: 0E-15
    val currencies =
      List(USD, ARS, AUD, BRL, CAD, CHF, CLP, CNY, CZK, DKK, EUR, GBP,
        HKD, INR, JPY, KRW, MXN, MYR, NOK, NZD, RUB, SEK, XAG, XAU)

    Arbitrary(
      for {
        n <- Arbitrary.arbitrary[Double]
        c <- Gen.oneOf(currencies)
      } yield Money(BigDecimal(n).setScale(c.formatDecimals, BigDecimal.RoundingMode.HALF_EVEN), c))
  }
}
