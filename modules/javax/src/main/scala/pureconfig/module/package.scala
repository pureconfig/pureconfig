package pureconfig.module

import _root_.javax.security.auth.kerberos.KerberosPrincipal
import _root_.javax.security.auth.x500.X500Principal

import scala.util.Try

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.fromStringConvertTry

/**
 * ConfigConvert instances for javax value classes.
 */
package object javax {
  implicit val readKerberosPrincipal: ConfigConvert[KerberosPrincipal] =
    fromStringConvertTry[KerberosPrincipal](s => Try(new KerberosPrincipal(s)), _.toString)

  implicit val readX500Principal: ConfigConvert[X500Principal] =
    fromStringConvertTry[X500Principal](s => Try(new X500Principal(s)), _.toString)
}
