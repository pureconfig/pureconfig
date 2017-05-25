package pureconfig.module.squants

import scala.reflect.ClassTag
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import org.scalatest.prop.PropertyChecks
import pureconfig.ConfigConvert
import pureconfig.syntax._
import _root_.squants._
import _root_.squants.market._
import com.typesafe.config.ConfigFactory

class SquantsConvertTest extends FlatSpec with Matchers with EitherValues with PropertyChecks {

  checkDimension(electro.Capacitance)
  checkDimension(electro.Conductivity)
  checkDimension(electro.ElectricCharge)
  checkDimension(electro.ElectricCurrent)
  checkDimension(electro.ElectricPotential)
  checkDimension(electro.ElectricalConductance)
  checkDimension(electro.ElectricalResistance)
  checkDimension(electro.Inductance)
  checkDimension(electro.MagneticFlux)
  checkDimension(electro.MagneticFluxDensity)
  checkDimension(electro.Resistivity)

  checkDimension(energy.Energy)
  checkDimension(energy.EnergyDensity)
  checkDimension(energy.Power)
  checkDimension(energy.PowerRamp)
  checkDimension(energy.SpecificEnergy)

  checkDimension(information.Information)
  checkDimension(information.DataRate)

  checkDimension(mass.AreaDensity)
  checkDimension(mass.ChemicalAmount)
  checkDimension(mass.Density)
  checkDimension(mass.Mass)
  checkDimension(mass.MomentOfInertia)

  checkDimension(motion.Acceleration)
  checkDimension(motion.AngularAcceleration)
  checkDimension(motion.AngularVelocity)
  checkDimension(motion.Force)
  checkDimension(motion.Jerk)
  checkDimension(motion.MassFlow)
  checkDimension(motion.Momentum)
  checkDimension(motion.Pressure)
  checkDimension(motion.PressureChange)
  checkDimension(motion.Torque)
  checkDimension(motion.Velocity)
  checkDimension(motion.VolumeFlow)
  checkDimension(motion.Yank)

  checkDimension(photo.Illuminance)
  checkDimension(photo.Luminance)
  checkDimension(photo.LuminousEnergy)
  checkDimension(photo.LuminousExposure)
  checkDimension(photo.LuminousFlux)
  checkDimension(photo.LuminousIntensity)

  checkDimension(radio.Irradiance)
  checkDimension(radio.Radiance)
  checkDimension(radio.SpectralIntensity)
  checkDimension(radio.SpectralIrradiance)
  checkDimension(radio.SpectralPower)

  checkDimension(space.Angle)
  checkDimension(space.Area)
  checkDimension(space.Length)
  checkDimension(space.SolidAngle)
  checkDimension(space.Volume)

  checkDimension(thermal.Temperature)
  checkDimension(thermal.ThermalCapacity)

  checkDimension(time.Frequency)
  checkDimension(time.Time)

  it should "parse Money" in forAll { (m: Money) =>
    checkConfig(SquantConfig(m))
  }

  case class SquantConfig[T](value: T)

  def checkDimension[T <: Quantity[T]](dim: Dimension[T])(implicit tag: ClassTag[T], cc: ConfigConvert[T]): Unit = {
    implicit val arbitrary = quantityAbitrary(dim)

    it should s"""parse ${tag.runtimeClass.getSimpleName}""" in forAll { (t: T) =>
      checkConfig(SquantConfig(t))
    }
  }

  def checkConfig[T](config: SquantConfig[T])(implicit cc: ConfigConvert[T]) = {
    val configString = s"""{value:"${config.value.toString}"}"""
    ConfigFactory.parseString(configString).to[SquantConfig[T]].right.value shouldEqual config
  }

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
