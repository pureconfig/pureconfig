package pureconfig
package generic

import scala.deriving.Mirror

inline def deriveForMirroredType[A](using m: Mirror.Of[A]): ConfigReader[A] =
  inline m match {
    case given Mirror.ProductOf[A] => deriveForMirroredProduct[A]
    case given Mirror.SumOf[A] => deriveForMirroredSum[A]
  }
