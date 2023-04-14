package pureconfig.module.doobie

import _root_.doobie.enumerated._
import _root_.doobie.hikari._

import pureconfig._
import pureconfig.generic.semiauto._

package object hikari {

  implicit lazy val readerTransactionIsolation: ConfigReader[TransactionIsolation] =
    deriveEnumerationReader(ConfigFieldMapping(ScreamingSnakeCase, ScreamingSnakeCase))

  implicit lazy val readerHikari: ConfigReader[Config] =
    deriveReader

}
