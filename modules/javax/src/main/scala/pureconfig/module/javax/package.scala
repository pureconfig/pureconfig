package pureconfig.module

import _root_.javax.security.auth.kerberos.KerberosPrincipal
import _root_.javax.security.auth.x500.X500Principal

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{catchReadError, viaString}

/** ConfigConvert instances for javax value classes.
  */
package object javax {
  implicit val readKerberosPrincipal: ConfigConvert[KerberosPrincipal] =
    viaString[KerberosPrincipal](catchReadError(s => new KerberosPrincipal(s)), _.toString)

  implicit val readX500Principal: ConfigConvert[X500Principal] =
    viaString[X500Principal](catchReadError(s => new X500Principal(s)), _.toString)
}
