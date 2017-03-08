package pureconfig.module

import _root_.javax.security.auth.kerberos.KerberosPrincipal

import scala.util.Try

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.fromStringConvertTry

/**
 * ConfigConvert instances for javax value classes.
 */
package object javax {
  implicit val readKerberosPrincipal: ConfigConvert[KerberosPrincipal] =
    fromStringConvertTry[KerberosPrincipal](s => Try(new KerberosPrincipal(s)), _.toString)
}
