package pureconfig.module

import scala.util.Try

import _root_.org.apache.hadoop.fs.Path

import pureconfig.ConfigConvert

/** `ConfigConvert` instances for Hadoop data structures.
  */
package object hadoop {

  implicit val pathConvert: ConfigConvert[Path] =
    ConfigConvert.viaNonEmptyStringTry[Path](s => Try(new Path(s)), _.toString)
}
