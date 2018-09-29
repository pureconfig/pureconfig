package pureconfig

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, Paths }

object PathUtils {

  def createTempFile(content: String): Path = {
    val path = Files.createTempFile("pureconfig", "conf")
    path.toFile.deleteOnExit()
    val writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)
    writer.write(content)
    writer.close()
    path
  }

  lazy val nonExistingPath: Path = {
    val path = Files.createTempFile("pureconfig", "conf")
    Files.delete(path)
    path
  }

  def resourceFromName(name: String): Path = {
    Paths.get(getClass.getResource(name).getPath)
  }

  def listResourcesFromNames(names: String*): Seq[Path] = names.map(resourceFromName)
}
