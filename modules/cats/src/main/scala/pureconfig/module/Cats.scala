package pureconfig.module

import pureconfig.error.{ ConfigReaderFailure, ConfigValueLocation }

object Cats {

  /**
   * A failure representing an unexpected empty traversable
   *
   * @param typ the type that was attempted to be converted to from an empty string
   * @param location an optional location of the ConfigValue that raised the
   *                 failure
   * @param path an optional path to the value which was an unexpected empty string
   */
  final case class EmptyTraversableFound(typ: String, location: Option[ConfigValueLocation], path: Option[String]) extends ConfigReaderFailure {
    def description = s"Empty collection found when trying to convert to $typ."

    def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
      this.copy(location = location orElse parentLocation, path = path.map(parentKey + "." + _) orElse Some(parentKey))
  }

}
