package pureconfig.module.squants

import scala.reflect.ClassTag

import com.typesafe.config.ConfigFactory
import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.{ BaseSuite, ConfigConvert }
import pureconfig.generic.auto._
import pureconfig.module.squants.arbitrary._
import pureconfig.syntax._
import _root_.squants._
import _root_.squants.market._

class SquantsConvertTest extends BaseSuite {

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

  {
    implicit val mc: MoneyContext = defaultMoneyContext
    checkArbitrary[Money]
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
    ConfigFactory.parseString(configString).to[SquantConfig[T]] shouldEqual Right(config)
  }
}
