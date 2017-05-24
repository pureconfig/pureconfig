package pureconfig.module.cats

import pureconfig.error.{ ConvertFailure, ConfigValueLocation }

/**
 * A failure representing an unexpected empty traversable
 *
 * @param typ the type that was attempted to be converted to from an empty string
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value which was an unexpected empty string
 */
final case class EmptyTraversableFound(typ: String, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"Empty collection found when trying to convert to $typ."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}
