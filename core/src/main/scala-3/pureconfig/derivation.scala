package pureconfig

import scala.deriving.Mirror

import pureconfig.generic.derivation.{CoproductConfigReader, ProductConfigReader}

extension (c: ConfigReader.type)
  inline def derived[A](using m: Mirror.Of[A]): ConfigReader[A] =
    inline m match {
      case given Mirror.ProductOf[A] => ProductConfigReader.derived[A]
      case given Mirror.SumOf[A] => CoproductConfigReader.derived[A]
    }
