package pureconfig.module

import _root_.squants.electro._
import _root_.squants.energy._
import _root_.squants.information._
import _root_.squants.market._
import _root_.squants.mass._
import _root_.squants.motion._
import _root_.squants.photo._
import _root_.squants.radio._
import _root_.squants.space._
import _root_.squants.thermal._
import _root_.squants.time._

import pureconfig.ConfigConvert._

/**
 * Provides [[ConfigConvert]] instances for Squants [[_root_.squants.Dimension]].
 *
 * Note: All of the machinery can disappear if the `parse` method of [[_root_.squants.Dimension]]
 * is made public. (see: https://github.com/typelevel/squants/issues/184). After that, something
 * like this should suffice:
 *
 * {{{
 *   implicit def dimensionConfigConvert[A <: Quantity[A]](dim: Dimension[A])(implicit tag: ClassTag[A]) =
 *     viaNonEmptyStringTry[A](dim.parse, _.toString)
 * }}}
 */
package object squants {

  // electro
  implicit val capacitanceConfigConvert =
    viaNonEmptyStringTry[Capacitance](Capacitance.apply, _.toString)
  implicit val conductivityConfigConvert =
    viaNonEmptyStringTry[Conductivity](Conductivity.apply, _.toString)
  implicit val electricChargeConfigConvert =
    viaNonEmptyStringTry[ElectricCharge](ElectricCharge.apply, _.toString)
  implicit val electricCurrentConfigConvert =
    viaNonEmptyStringTry[ElectricCurrent](ElectricCurrent.apply, _.toString)
  implicit val electricPotentialConfigConvert =
    viaNonEmptyStringTry[ElectricPotential](ElectricPotential.apply, _.toString)
  implicit val electricalConductanceConfigConvert =
    viaNonEmptyStringTry[ElectricalConductance](ElectricalConductance.apply, _.toString)
  implicit val electricalResistanceConfigConvert =
    viaNonEmptyStringTry[ElectricalResistance](ElectricalResistance.apply, _.toString)
  implicit val inductanceConfigConvert =
    viaNonEmptyStringTry[Inductance](Inductance.apply, _.toString)
  implicit val magneticFluxConfigConvert =
    viaNonEmptyStringTry[MagneticFlux](MagneticFlux.apply, _.toString)
  implicit val magneticFluxDensityConfigConvert =
    viaNonEmptyStringTry[MagneticFluxDensity](MagneticFluxDensity.apply, _.toString)
  implicit val resistivityConfigConvert =
    viaNonEmptyStringTry[Resistivity](Resistivity.apply, _.toString)

  // energy
  implicit val energyConfigConvert =
    viaNonEmptyStringTry[Energy](Energy.apply, _.toString)
  implicit val energyDensityConfigConvert =
    viaNonEmptyStringTry[EnergyDensity](EnergyDensity.apply, _.toString)
  implicit val powerConfigConvert =
    viaNonEmptyStringTry[Power](Power.apply, _.toString)
  implicit val powerRampConfigConvert =
    viaNonEmptyStringTry[PowerRamp](PowerRamp.apply, _.toString)
  implicit val SpecificEnergyConfigConvert =
    viaNonEmptyStringTry[SpecificEnergy](SpecificEnergy.apply, _.toString)

  // information
  implicit val informationConfigConvert =
    viaNonEmptyStringTry[Information](Information.apply, _.toString)
  implicit val dataRateConfigConvert =
    viaNonEmptyStringTry[DataRate](DataRate.apply, _.toString)

  // market
  // Using own string representation due to https://github.com/typelevel/squants/issues/321
  implicit def moneyDensityConfigConvert(implicit mc: MoneyContext) =
    viaNonEmptyStringTry[Money](Money.apply, { m => m.amount.underlying.toPlainString + " " + m.currency.code })

  // mass
  implicit val areaDensityConfigConvert =
    viaNonEmptyStringTry[AreaDensity](AreaDensity.apply, _.toString)
  implicit val chemicalAmountConfigConvert =
    viaNonEmptyStringTry[ChemicalAmount](ChemicalAmount.apply, _.toString)
  implicit val densityConfigConvert =
    viaNonEmptyStringTry[Density](Density.apply, _.toString)
  implicit val massConfigConvert =
    viaNonEmptyStringTry[Mass](Mass.apply, _.toString)
  implicit val momentOfInertiaConfigConvert =
    viaNonEmptyStringTry[MomentOfInertia](MomentOfInertia.apply, _.toString)

  // motion
  implicit val accelerationConfigConvert =
    viaNonEmptyStringTry[Acceleration](Acceleration.apply, _.toString)
  implicit val angularAccelerationConfigConvert =
    viaNonEmptyStringTry[AngularAcceleration](AngularAcceleration.apply, _.toString)
  implicit val angularVelocityConfigConvert =
    viaNonEmptyStringTry[AngularVelocity](AngularVelocity.apply, _.toString)
  implicit val forceConfigConvert =
    viaNonEmptyStringTry[Force](Force.apply, _.toString)
  implicit val jerkConfigConvert =
    viaNonEmptyStringTry[Jerk](Jerk.apply, _.toString)
  implicit val massFlowConfigConvert =
    viaNonEmptyStringTry[MassFlow](MassFlow.apply, _.toString)
  implicit val momentumConfigConvert =
    viaNonEmptyStringTry[Momentum](Momentum.apply, _.toString)
  implicit val pressureConfigConvert =
    viaNonEmptyStringTry[Pressure](Pressure.apply, _.toString)
  implicit val pressureChangeConfigConvert =
    viaNonEmptyStringTry[PressureChange](PressureChange.apply, _.toString)
  implicit val torqueConfigConvert =
    viaNonEmptyStringTry[Torque](Torque.apply, _.toString)
  implicit val velocityConfigConvert =
    viaNonEmptyStringTry[Velocity](Velocity.apply, _.toString)
  implicit val volumeFlowConfigConvert =
    viaNonEmptyStringTry[VolumeFlow](VolumeFlow.apply, _.toString)
  implicit val yankConfigConvert =
    viaNonEmptyStringTry[Yank](Yank.apply, _.toString)

  // photo
  implicit val illuminanceConfigConvert =
    viaNonEmptyStringTry[Illuminance](Illuminance.apply, _.toString)
  implicit val luminanceConfigConvert =
    viaNonEmptyStringTry[Luminance](Luminance.apply, _.toString)
  implicit val luminousEnergyConfigConvert =
    viaNonEmptyStringTry[LuminousEnergy](LuminousEnergy.apply, _.toString)
  implicit val luminousExposureConfigConvert =
    viaNonEmptyStringTry[LuminousExposure](LuminousExposure.apply, _.toString)
  implicit val luminousFluxConfigConvert =
    viaNonEmptyStringTry[LuminousFlux](LuminousFlux.apply, _.toString)
  implicit val luminousIntensityConfigConvert =
    viaNonEmptyStringTry[LuminousIntensity](LuminousIntensity.apply, _.toString)

  // radio
  implicit val irradianceConfigConvert =
    viaNonEmptyStringTry[Irradiance](Irradiance.apply, _.toString)
  implicit val radianceConfigConvert =
    viaNonEmptyStringTry[Radiance](Radiance.apply, _.toString)
  implicit val spectralIntensityConfigConvert =
    viaNonEmptyStringTry[SpectralIntensity](SpectralIntensity.apply, _.toString)
  implicit val spectralIrradianceConfigConvert =
    viaNonEmptyStringTry[SpectralIrradiance](SpectralIrradiance.apply, _.toString)
  implicit val spectralPowerConfigConvert =
    viaNonEmptyStringTry[SpectralPower](SpectralPower.apply, _.toString)

  // space
  implicit val angleConfigConvert =
    viaNonEmptyStringTry[Angle](Angle.apply, _.toString)
  implicit val areaConfigConvert =
    viaNonEmptyStringTry[Area](Area.apply, _.toString)
  implicit val lengthConfigConvert =
    viaNonEmptyStringTry[Length](Length.apply, _.toString)
  implicit val solidAngleConfigConvert =
    viaNonEmptyStringTry[SolidAngle](SolidAngle.apply, _.toString)
  implicit val volumeConfigConvert =
    viaNonEmptyStringTry[Volume](Volume.apply, _.toString)

  // thermal
  implicit val temperatureConfigConvert =
    viaNonEmptyStringTry[Temperature](Temperature.apply, _.toString)
  implicit val thermalCapacityConfigConvert =
    viaNonEmptyStringTry[ThermalCapacity](ThermalCapacity.apply, _.toString)

  // time
  implicit val frequencyConfigConvert =
    viaNonEmptyStringTry[Frequency](Frequency.apply, _.toString)
  implicit val timeConfigConvert =
    viaNonEmptyStringTry[Time](Time.apply, _.toString)

}
