package pureconfig

import scala.deriving.Mirror

import pureconfig.generic.derivation.*

trait ReaderDerives {
  inline def derived[A](using m: Mirror.Of[A]): ConfigReader[A] =
    ConfigReaderDerivation.Default.deriveConfigReader[A]
}
