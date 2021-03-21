package pureconfig

import scala.deriving.Mirror

import pureconfig.generic.deriveForMirroredType

extension (c: ConfigReader.type)
  inline def derived[A](using Mirror.Of[A]) =
    deriveForMirroredType
