package pureconfig.module.squants

import scala.reflect.runtime.universe._

import _root_.squants._
import _root_.squants.market._

import pureconfig.module.squants.arbitrary._
import pureconfig.{BaseSuite, ConfigConvert}

class SquantsConvertTest extends BaseSuite {
  implicit val mc: MoneyContext = defaultMoneyContext

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

  checkArbitrary[market.Money]

  def checkDimension[T <: Quantity[T]](
      dim: Dimension[T]
  )(implicit tag: TypeTag[T], cc: ConfigConvert[T]): Unit = {
    implicit val arbitrary = quantityAbitrary(dim)
    checkArbitrary[T]
  }
}
