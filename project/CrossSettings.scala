import sbt._

object CrossSettings {
  def crossSettings[T](scalaVersion: String, if3: List[T], if2: List[T]) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((3, _)) => if3
      case Some((2, 12 | 13)) => if2
      case _ => Nil
    }
}
