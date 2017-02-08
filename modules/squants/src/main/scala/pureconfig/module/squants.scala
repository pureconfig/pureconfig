package pureconfig.module

import scala.util.{ Failure, Success, Try }

import pureconfig.ConfigConvert

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
 *     ConfigConvert.nonEmptyStringConvert[T](dim.parse, _.toString)
 * }}}
 */
package object squants {

  // electro
  implicit val capacitanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Capacitance](Capacitance.apply, _.toString)
  implicit val conductivityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Conductivity](Conductivity.apply, _.toString)
  implicit val electricChargeConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ElectricCharge](ElectricCharge.apply, _.toString)
  implicit val electricCurrentConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ElectricCurrent](ElectricCurrent.apply, _.toString)
  implicit val electricPotentialConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ElectricPotential](ElectricPotential.apply, _.toString)
  implicit val electricalConductanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ElectricalConductance](ElectricalConductance.apply, _.toString)
  implicit val electricalResistanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ElectricalResistance](ElectricalResistance.apply, _.toString)
  implicit val inductanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Inductance](Inductance.apply, _.toString)
  implicit val magneticFluxConfigConvert =
    ConfigConvert.nonEmptyStringConvert[MagneticFlux](MagneticFlux.apply, _.toString)
  implicit val magneticFluxDensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[MagneticFluxDensity](MagneticFluxDensity.apply, _.toString)
  implicit val resistivityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Resistivity](Resistivity.apply, _.toString)

  // energy
  implicit val energyConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Energy](Energy.apply, _.toString)
  implicit val energyDensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[EnergyDensity](EnergyDensity.apply, _.toString)
  implicit val powerConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Power](Power.apply, _.toString)
  implicit val powerRampConfigConvert =
    ConfigConvert.nonEmptyStringConvert[PowerRamp](PowerRamp.apply, _.toString)
  implicit val SpecificEnergyConfigConvert =
    ConfigConvert.nonEmptyStringConvert[SpecificEnergy](SpecificEnergy.apply, _.toString)

  // information
  implicit val informationConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Information](Information.apply, _.toString)
  implicit val dataRateConfigConvert =
    ConfigConvert.nonEmptyStringConvert[DataRate](DataRate.apply, _.toString)

  // market
  implicit val moneyDensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Money](Money.apply, _.toString)

  // mass
  implicit val areaDensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[AreaDensity](AreaDensity.apply, _.toString)
  implicit val chemicalAmountConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ChemicalAmount](ChemicalAmount.apply, _.toString)
  implicit val densityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Density](Density.apply, _.toString)
  implicit val massConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Mass](Mass.apply, _.toString)

  // motion
  implicit val accelerationConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Acceleration](Acceleration.apply, _.toString)
  implicit val angularVelocityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[AngularVelocity](AngularVelocity.apply, _.toString)
  implicit val forceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Force](Force.apply, _.toString)
  implicit val jerkConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Jerk](Jerk.apply, _.toString)
  implicit val massFlowConfigConvert =
    ConfigConvert.nonEmptyStringConvert[MassFlow](MassFlow.apply, _.toString)
  implicit val momentumConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Momentum](Momentum.apply, _.toString)
  implicit val pressureConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Pressure](Pressure.apply, _.toString)
  implicit val pressureChangeConfigConvert =
    ConfigConvert.nonEmptyStringConvert[PressureChange](PressureChange.apply, _.toString)
  implicit val velocityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Velocity](Velocity.apply, _.toString)
  implicit val volumeFlowConfigConvert =
    ConfigConvert.nonEmptyStringConvert[VolumeFlow](VolumeFlow.apply, _.toString)
  implicit val yankConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Yank](Yank.apply, _.toString)

  // photo
  implicit val illuminanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Illuminance](Illuminance.apply, _.toString)
  implicit val luminanceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Luminance](Luminance.apply, _.toString)
  implicit val luminousEnergyConfigConvert =
    ConfigConvert.nonEmptyStringConvert[LuminousEnergy](LuminousEnergy.apply, _.toString)
  implicit val luminousExposureConfigConvert =
    ConfigConvert.nonEmptyStringConvert[LuminousExposure](LuminousExposure.apply, _.toString)
  implicit val luminousFluxConfigConvert =
    ConfigConvert.nonEmptyStringConvert[LuminousFlux](LuminousFlux.apply, _.toString)
  implicit val luminousIntensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[LuminousIntensity](LuminousIntensity.apply, _.toString)

  // radio
  implicit val irradianceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Irradiance](Irradiance.apply, _.toString)
  implicit val radianceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Radiance](Radiance.apply, _.toString)
  implicit val spectralIntensityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[SpectralIntensity](SpectralIntensity.apply, _.toString)
  implicit val spectralIrradianceConfigConvert =
    ConfigConvert.nonEmptyStringConvert[SpectralIrradiance](SpectralIrradiance.apply, _.toString)
  implicit val spectralPowerConfigConvert =
    ConfigConvert.nonEmptyStringConvert[SpectralPower](SpectralPower.apply, _.toString)

  // space
  implicit val angleConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Angle](Angle.apply, _.toString)
  implicit val areaConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Area](Area.apply, _.toString)
  implicit val lengthConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Length](Length.apply, _.toString)
  implicit val solidAngleConfigConvert =
    ConfigConvert.nonEmptyStringConvert[SolidAngle](SolidAngle.apply, _.toString)
  implicit val volumeConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Volume](Volume.apply, _.toString)

  // thermal
  implicit val temperatureConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Temperature](parseTemperature, _.toString)
  implicit val thermalCapacityConfigConvert =
    ConfigConvert.nonEmptyStringConvert[ThermalCapacity](ThermalCapacity.apply, _.toString)

  // time
  implicit val frequencyConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Frequency](Frequency.apply, _.toString)
  implicit val timeConfigConvert =
    ConfigConvert.nonEmptyStringConvert[Time](Time.apply, _.toString)

  // This is temporary until https://github.com/typelevel/squants/pull/183 is released (1.2.0 ???)
  // Without it, ScalaCheck has a pretty easy time breaking the conversion
  private[squants] def parseTemperature(s: String): Try[Temperature] = {
    val regex = "([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)[ °]*(f|F|c|C|k|K|r|R)".r
    s match {
      case regex(value, unit) => unit match {
        case Fahrenheit.symbol | "f" | "F" => Success(Fahrenheit(value.toDouble))
        case Celsius.symbol | "c" | "C" => Success(Celsius(value.toDouble))
        case Kelvin.symbol | "k" | "K" => Success(Kelvin(value.toDouble))
        case Rankine.symbol | "r" | "R" => Success(Rankine(value.toDouble))
        case unknownUnit => Failure(new Exception(s"PureConfig developer error: Regex '$regex' found unit '$unknownUnit' which was not handled."))
      }
      case _ => Failure(QuantityParseException("Unable to parse Temperature", s))
    }
  }
}
