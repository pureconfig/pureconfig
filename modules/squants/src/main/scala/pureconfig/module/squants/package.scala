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

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert._

/** Provides [[ConfigConvert]] instances for Squants [[_root_.squants.Dimension]].
  *
  * Note: All of the machinery can disappear if the `parse` method of [[_root_.squants.Dimension]] is made public. (see:
  * https://github.com/typelevel/squants/issues/184). After that, something like this should suffice:
  *
  * {{{
  *   implicit def dimensionConfigConvert[A <: Quantity[A]](dim: Dimension[A])(implicit tag: ClassTag[A]) =
  *     viaNonEmptyStringTry[A](dim.parse, _.toString)
  * }}}
  */
package object squants {

  // electro
  implicit val capacitanceConfigConvert: ConfigConvert[Capacitance] =
    viaNonEmptyStringTry[Capacitance](Capacitance.apply, _.toString)
  implicit val conductivityConfigConvert: ConfigConvert[Conductivity] =
    viaNonEmptyStringTry[Conductivity](Conductivity.apply, _.toString)
  implicit val electricChargeConfigConvert: ConfigConvert[ElectricCharge] =
    viaNonEmptyStringTry[ElectricCharge](ElectricCharge.apply, _.toString)
  implicit val electricCurrentConfigConvert: ConfigConvert[ElectricCurrent] =
    viaNonEmptyStringTry[ElectricCurrent](ElectricCurrent.apply, _.toString)
  implicit val electricPotentialConfigConvert: ConfigConvert[ElectricPotential] =
    viaNonEmptyStringTry[ElectricPotential](ElectricPotential.apply, _.toString)
  implicit val electricalConductanceConfigConvert: ConfigConvert[ElectricalConductance] =
    viaNonEmptyStringTry[ElectricalConductance](ElectricalConductance.apply, _.toString)
  implicit val electricalResistanceConfigConvert: ConfigConvert[ElectricalResistance] =
    viaNonEmptyStringTry[ElectricalResistance](ElectricalResistance.apply, _.toString)
  implicit val inductanceConfigConvert: ConfigConvert[Inductance] =
    viaNonEmptyStringTry[Inductance](Inductance.apply, _.toString)
  implicit val magneticFluxConfigConvert: ConfigConvert[MagneticFlux] =
    viaNonEmptyStringTry[MagneticFlux](MagneticFlux.apply, _.toString)
  implicit val magneticFluxDensityConfigConvert: ConfigConvert[MagneticFluxDensity] =
    viaNonEmptyStringTry[MagneticFluxDensity](MagneticFluxDensity.apply, _.toString)
  implicit val resistivityConfigConvert: ConfigConvert[Resistivity] =
    viaNonEmptyStringTry[Resistivity](Resistivity.apply, _.toString)

  // energy
  implicit val energyConfigConvert: ConfigConvert[Energy] =
    viaNonEmptyStringTry[Energy](Energy.apply, _.toString)
  implicit val energyDensityConfigConvert: ConfigConvert[EnergyDensity] =
    viaNonEmptyStringTry[EnergyDensity](EnergyDensity.apply, _.toString)
  implicit val powerConfigConvert: ConfigConvert[Power] =
    viaNonEmptyStringTry[Power](Power.apply, _.toString)
  implicit val powerRampConfigConvert: ConfigConvert[PowerRamp] =
    viaNonEmptyStringTry[PowerRamp](PowerRamp.apply, _.toString)
  implicit val SpecificEnergyConfigConvert: ConfigConvert[SpecificEnergy] =
    viaNonEmptyStringTry[SpecificEnergy](SpecificEnergy.apply, _.toString)

  // information
  implicit val informationConfigConvert: ConfigConvert[Information] =
    viaNonEmptyStringTry[Information](Information.apply, _.toString)
  implicit val dataRateConfigConvert: ConfigConvert[DataRate] =
    viaNonEmptyStringTry[DataRate](DataRate.apply, _.toString)

  // market
  // Using own string representation due to https://github.com/typelevel/squants/issues/321
  implicit def moneyDensityConfigConvert(implicit mc: MoneyContext): ConfigConvert[Money] =
    viaNonEmptyStringTry[Money](Money.apply, { m => m.amount.underlying.toPlainString + " " + m.currency.code })

  // mass
  implicit val areaDensityConfigConvert: ConfigConvert[AreaDensity] =
    viaNonEmptyStringTry[AreaDensity](AreaDensity.apply, _.toString)
  implicit val chemicalAmountConfigConvert: ConfigConvert[ChemicalAmount] =
    viaNonEmptyStringTry[ChemicalAmount](ChemicalAmount.apply, _.toString)
  implicit val densityConfigConvert: ConfigConvert[Density] =
    viaNonEmptyStringTry[Density](Density.apply, _.toString)
  implicit val massConfigConvert: ConfigConvert[Mass] =
    viaNonEmptyStringTry[Mass](Mass.apply, _.toString)
  implicit val momentOfInertiaConfigConvert: ConfigConvert[MomentOfInertia] =
    viaNonEmptyStringTry[MomentOfInertia](MomentOfInertia.apply, _.toString)

  // motion
  implicit val accelerationConfigConvert: ConfigConvert[Acceleration] =
    viaNonEmptyStringTry[Acceleration](Acceleration.apply, _.toString)
  implicit val angularAccelerationConfigConvert: ConfigConvert[AngularAcceleration] =
    viaNonEmptyStringTry[AngularAcceleration](AngularAcceleration.apply, _.toString)
  implicit val angularVelocityConfigConvert: ConfigConvert[AngularVelocity] =
    viaNonEmptyStringTry[AngularVelocity](AngularVelocity.apply, _.toString)
  implicit val forceConfigConvert: ConfigConvert[Force] =
    viaNonEmptyStringTry[Force](Force.apply, _.toString)
  implicit val jerkConfigConvert: ConfigConvert[Jerk] =
    viaNonEmptyStringTry[Jerk](Jerk.apply, _.toString)
  implicit val massFlowConfigConvert: ConfigConvert[MassFlow] =
    viaNonEmptyStringTry[MassFlow](MassFlow.apply, _.toString)
  implicit val momentumConfigConvert: ConfigConvert[Momentum] =
    viaNonEmptyStringTry[Momentum](Momentum.apply, _.toString)
  implicit val pressureConfigConvert: ConfigConvert[Pressure] =
    viaNonEmptyStringTry[Pressure](Pressure.apply, _.toString)
  implicit val pressureChangeConfigConvert: ConfigConvert[PressureChange] =
    viaNonEmptyStringTry[PressureChange](PressureChange.apply, _.toString)
  implicit val torqueConfigConvert: ConfigConvert[Torque] =
    viaNonEmptyStringTry[Torque](Torque.apply, _.toString)
  implicit val velocityConfigConvert: ConfigConvert[Velocity] =
    viaNonEmptyStringTry[Velocity](Velocity.apply, _.toString)
  implicit val volumeFlowConfigConvert: ConfigConvert[VolumeFlow] =
    viaNonEmptyStringTry[VolumeFlow](VolumeFlow.apply, _.toString)
  implicit val yankConfigConvert: ConfigConvert[Yank] =
    viaNonEmptyStringTry[Yank](Yank.apply, _.toString)

  // photo
  implicit val illuminanceConfigConvert: ConfigConvert[Illuminance] =
    viaNonEmptyStringTry[Illuminance](Illuminance.apply, _.toString)
  implicit val luminanceConfigConvert: ConfigConvert[Luminance] =
    viaNonEmptyStringTry[Luminance](Luminance.apply, _.toString)
  implicit val luminousEnergyConfigConvert: ConfigConvert[LuminousEnergy] =
    viaNonEmptyStringTry[LuminousEnergy](LuminousEnergy.apply, _.toString)
  implicit val luminousExposureConfigConvert: ConfigConvert[LuminousExposure] =
    viaNonEmptyStringTry[LuminousExposure](LuminousExposure.apply, _.toString)
  implicit val luminousFluxConfigConvert: ConfigConvert[LuminousFlux] =
    viaNonEmptyStringTry[LuminousFlux](LuminousFlux.apply, _.toString)
  implicit val luminousIntensityConfigConvert: ConfigConvert[LuminousIntensity] =
    viaNonEmptyStringTry[LuminousIntensity](LuminousIntensity.apply, _.toString)

  // radio
  implicit val irradianceConfigConvert: ConfigConvert[Irradiance] =
    viaNonEmptyStringTry[Irradiance](Irradiance.apply, _.toString)
  implicit val radianceConfigConvert: ConfigConvert[Radiance] =
    viaNonEmptyStringTry[Radiance](Radiance.apply, _.toString)
  implicit val spectralIntensityConfigConvert: ConfigConvert[SpectralIntensity] =
    viaNonEmptyStringTry[SpectralIntensity](SpectralIntensity.apply, _.toString)
  implicit val spectralIrradianceConfigConvert: ConfigConvert[SpectralIrradiance] =
    viaNonEmptyStringTry[SpectralIrradiance](SpectralIrradiance.apply, _.toString)
  implicit val spectralPowerConfigConvert: ConfigConvert[SpectralPower] =
    viaNonEmptyStringTry[SpectralPower](SpectralPower.apply, _.toString)

  // space
  implicit val angleConfigConvert: ConfigConvert[Angle] =
    viaNonEmptyStringTry[Angle](Angle.apply, _.toString)
  implicit val areaConfigConvert: ConfigConvert[Area] =
    viaNonEmptyStringTry[Area](Area.apply, _.toString)
  implicit val lengthConfigConvert: ConfigConvert[Length] =
    viaNonEmptyStringTry[Length](Length.apply, _.toString)
  implicit val solidAngleConfigConvert: ConfigConvert[SolidAngle] =
    viaNonEmptyStringTry[SolidAngle](SolidAngle.apply, _.toString)
  implicit val volumeConfigConvert: ConfigConvert[Volume] =
    viaNonEmptyStringTry[Volume](Volume.apply, _.toString)

  // thermal
  implicit val temperatureConfigConvert: ConfigConvert[Temperature] =
    viaNonEmptyStringTry[Temperature](Temperature.apply, _.toString)
  implicit val thermalCapacityConfigConvert: ConfigConvert[ThermalCapacity] =
    viaNonEmptyStringTry[ThermalCapacity](ThermalCapacity.apply, _.toString)

  // time
  implicit val frequencyConfigConvert: ConfigConvert[Frequency] =
    viaNonEmptyStringTry[Frequency](Frequency.apply, _.toString)
  implicit val timeConfigConvert: ConfigConvert[Time] =
    viaNonEmptyStringTry[Time](Time.apply, _.toString)

}
