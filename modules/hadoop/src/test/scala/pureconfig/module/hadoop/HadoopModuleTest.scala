package pureconfig.module.hadoop

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import org.apache.hadoop.fs.Path
import pureconfig.{BaseSuite, ConfigWriter}
import pureconfig.error.{CannotConvert, EmptyStringFound}
import pureconfig.generic.auto._
import pureconfig.syntax._

class HadoopModuleTest extends BaseSuite {

  case class Conf(path: Path)

  behavior of "HDFS Path ConfigConvert"

  it should "be able to read correct path value from config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    val expected = new Path(strPath)
    val config = ConfigFactory.parseString(s"""{ path: "$strPath" }""")
    config.to[Conf].value shouldEqual Conf(expected)
  }

  it should "be able to write correct path value to config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    val conf = Conf(new Path(strPath))
    val expected = ConfigFactory.parseString(s"""{ path: "$strPath" }""").root().render(ConfigRenderOptions.concise())
    ConfigWriter[Conf].to(conf).render(ConfigRenderOptions.concise()) shouldEqual expected
  }

  it should "refuse an invalid path value" in {
    val config = ConfigFactory.parseString(s"""{ path: "file:#//123" }""")
    config.to[Conf] should failWithConvertFailureOf[CannotConvert]
  }

  it should "refuse an empty path value" in {
    val config = ConfigFactory.parseString(s"""{ path: "" }""")
    config.to[Conf] should failWithConvertFailureOf[EmptyStringFound]
  }
}
