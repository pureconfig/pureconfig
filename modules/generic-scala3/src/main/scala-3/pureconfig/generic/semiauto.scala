package pureconfig.generic

import scala.deriving.Mirror

import pureconfig.ConfigReader
import pureconfig.generic.derivation.*

object semiauto:
  export reader.deriveReader