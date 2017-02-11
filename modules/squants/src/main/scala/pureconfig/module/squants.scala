package pureconfig.module

import scala.util.{ Failure, Success, Try }

import pureconfig.ConfigConvert._

import _root_.squants.QuantityParseException
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

/**
 * Provides [[ConfigConvert]] instances for Squants [[_root_.squants.Dimension]].
 *
 * Note: All of the machinery can disappear if the `parse` method of [[_root_.squants.Dimension]]
 * is made public. (see: https://github.com/typelevel/squants/issues/184). After that, something
 * like this should suffice:
 *
 * {{{
 *   implicit def dimensionConfigConvert[T <: Quantity[T]](dim: Dimension[T])(implicit tag: ClassTag[T]) =
 *     fromNonEmptyStringConvertTry[T](dim.parse, _.toString)
 * }}}
 */
package object squants {

  // electro
  implicit val capacitanceConfigConvert =
    fromNonEmptyStringConvertTry[Capacitance](Capacitance.apply, _.toString)
  implicit val conductivityConfigConvert =
    fromNonEmptyStringConvertTry[Conductivity](Conductivity.apply, _.toString)
  implicit val electricChargeConfigConvert =
    fromNonEmptyStringConvertTry[ElectricCharge](ElectricCharge.apply, _.toString)
  implicit val electricCurrentConfigConvert =
    fromNonEmptyStringConvertTry[ElectricCurrent](ElectricCurrent.apply, _.toString)
  implicit val electricPotentialConfigConvert =
    fromNonEmptyStringConvertTry[ElectricPotential](ElectricPotential.apply, _.toString)
  implicit val electricalConductanceConfigConvert =
    fromNonEmptyStringConvertTry[ElectricalConductance](ElectricalConductance.apply, _.toString)
  implicit val electricalResistanceConfigConvert =
    fromNonEmptyStringConvertTry[ElectricalResistance](ElectricalResistance.apply, _.toString)
  implicit val inductanceConfigConvert =
    fromNonEmptyStringConvertTry[Inductance](Inductance.apply, _.toString)
  implicit val magneticFluxConfigConvert =
    fromNonEmptyStringConvertTry[MagneticFlux](MagneticFlux.apply, _.toString)
  implicit val magneticFluxDensityConfigConvert =
    fromNonEmptyStringConvertTry[MagneticFluxDensity](MagneticFluxDensity.apply, _.toString)
  implicit val resistivityConfigConvert =
    fromNonEmptyStringConvertTry[Resistivity](Resistivity.apply, _.toString)

  // energy
  implicit val energyConfigConvert =
    fromNonEmptyStringConvertTry[Energy](Energy.apply, _.toString)
  implicit val energyDensityConfigConvert =
    fromNonEmptyStringConvertTry[EnergyDensity](EnergyDensity.apply, _.toString)
  implicit val powerConfigConvert =
    fromNonEmptyStringConvertTry[Power](Power.apply, _.toString)
  implicit val powerRampConfigConvert =
    fromNonEmptyStringConvertTry[PowerRamp](PowerRamp.apply, _.toString)
  implicit val SpecificEnergyConfigConvert =
    fromNonEmptyStringConvertTry[SpecificEnergy](SpecificEnergy.apply, _.toString)

  // information
  implicit val informationConfigConvert =
    fromNonEmptyStringConvertTry[Information](Information.apply, _.toString)
  implicit val dataRateConfigConvert =
    fromNonEmptyStringConvertTry[DataRate](DataRate.apply, _.toString)

  // market
  implicit val moneyDensityConfigConvert =
    fromNonEmptyStringConvertTry[Money](Money.apply, _.toString)

  // mass
  implicit val areaDensityConfigConvert =
    fromNonEmptyStringConvertTry[AreaDensity](AreaDensity.apply, _.toString)
  implicit val chemicalAmountConfigConvert =
    fromNonEmptyStringConvertTry[ChemicalAmount](ChemicalAmount.apply, _.toString)
  implicit val densityConfigConvert =
    fromNonEmptyStringConvertTry[Density](Density.apply, _.toString)
  implicit val massConfigConvert =
    fromNonEmptyStringConvertTry[Mass](Mass.apply, _.toString)

  // motion
  implicit val accelerationConfigConvert =
    fromNonEmptyStringConvertTry[Acceleration](Acceleration.apply, _.toString)
  implicit val angularVelocityConfigConvert =
    fromNonEmptyStringConvertTry[AngularVelocity](AngularVelocity.apply, _.toString)
  implicit val forceConfigConvert =
    fromNonEmptyStringConvertTry[Force](Force.apply, _.toString)
  implicit val jerkConfigConvert =
    fromNonEmptyStringConvertTry[Jerk](Jerk.apply, _.toString)
  implicit val massFlowConfigConvert =
    fromNonEmptyStringConvertTry[MassFlow](MassFlow.apply, _.toString)
  implicit val momentumConfigConvert =
    fromNonEmptyStringConvertTry[Momentum](Momentum.apply, _.toString)
  implicit val pressureConfigConvert =
    fromNonEmptyStringConvertTry[Pressure](Pressure.apply, _.toString)
  implicit val pressureChangeConfigConvert =
    fromNonEmptyStringConvertTry[PressureChange](PressureChange.apply, _.toString)
  implicit val velocityConfigConvert =
    fromNonEmptyStringConvertTry[Velocity](Velocity.apply, _.toString)
  implicit val volumeFlowConfigConvert =
    fromNonEmptyStringConvertTry[VolumeFlow](VolumeFlow.apply, _.toString)
  implicit val yankConfigConvert =
    fromNonEmptyStringConvertTry[Yank](Yank.apply, _.toString)

  // photo
  implicit val illuminanceConfigConvert =
    fromNonEmptyStringConvertTry[Illuminance](Illuminance.apply, _.toString)
  implicit val luminanceConfigConvert =
    fromNonEmptyStringConvertTry[Luminance](Luminance.apply, _.toString)
  implicit val luminousEnergyConfigConvert =
    fromNonEmptyStringConvertTry[LuminousEnergy](LuminousEnergy.apply, _.toString)
  implicit val luminousExposureConfigConvert =
    fromNonEmptyStringConvertTry[LuminousExposure](LuminousExposure.apply, _.toString)
  implicit val luminousFluxConfigConvert =
    fromNonEmptyStringConvertTry[LuminousFlux](LuminousFlux.apply, _.toString)
  implicit val luminousIntensityConfigConvert =
    fromNonEmptyStringConvertTry[LuminousIntensity](LuminousIntensity.apply, _.toString)

  // radio
  implicit val irradianceConfigConvert =
    fromNonEmptyStringConvertTry[Irradiance](Irradiance.apply, _.toString)
  implicit val radianceConfigConvert =
    fromNonEmptyStringConvertTry[Radiance](Radiance.apply, _.toString)
  implicit val spectralIntensityConfigConvert =
    fromNonEmptyStringConvertTry[SpectralIntensity](SpectralIntensity.apply, _.toString)
  implicit val spectralIrradianceConfigConvert =
    fromNonEmptyStringConvertTry[SpectralIrradiance](SpectralIrradiance.apply, _.toString)
  implicit val spectralPowerConfigConvert =
    fromNonEmptyStringConvertTry[SpectralPower](SpectralPower.apply, _.toString)

  // space
  implicit val angleConfigConvert =
    fromNonEmptyStringConvertTry[Angle](Angle.apply, _.toString)
  implicit val areaConfigConvert =
    fromNonEmptyStringConvertTry[Area](Area.apply, _.toString)
  implicit val lengthConfigConvert =
    fromNonEmptyStringConvertTry[Length](Length.apply, _.toString)
  implicit val solidAngleConfigConvert =
    fromNonEmptyStringConvertTry[SolidAngle](SolidAngle.apply, _.toString)
  implicit val volumeConfigConvert =
    fromNonEmptyStringConvertTry[Volume](Volume.apply, _.toString)

  // thermal
  implicit val temperatureConfigConvert =
    fromNonEmptyStringConvertTry[Temperature](parseTemperature, _.toString)
  implicit val thermalCapacityConfigConvert =
    fromNonEmptyStringConvertTry[ThermalCapacity](ThermalCapacity.apply, _.toString)

  // time
  implicit val frequencyConfigConvert =
    fromNonEmptyStringConvertTry[Frequency](Frequency.apply, _.toString)
  implicit val timeConfigConvert =
    fromNonEmptyStringConvertTry[Time](Time.apply, _.toString)

  // This is temporary until https://github.com/typelevel/squants/pull/183 is released (1.2.0 ???)
  // Without it, ScalaCheck has a pretty easy time breaking the conversion
  private[squants] def parseTemperature(s: String): Try[Temperature] = {
    val regex = "([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)* *°? *(f|F|c|C|k|K|r|R)".r
    s match {
      case regex(value, unit) => unit match {
        case "f" | "F" => Success(Fahrenheit(value.toDouble))
        case "c" | "C" => Success(Celsius(value.toDouble))
        case "k" | "K" => Success(Kelvin(value.toDouble))
        case "r" | "R" => Success(Rankine(value.toDouble))
      }
      case _ => Failure(QuantityParseException("Unable to parse Temperature", s))
    }
  }
}
