package pureconfig.module.hadoop

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import org.apache.hadoop.fs.Path

import pureconfig.error.{CannotConvert, EmptyStringFound}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigSource, ConfigWriter}

class HadoopModuleTest extends BaseSuite {

  behavior of "HDFS Path ConfigConvert"

  it should "be able to read correct path value from config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    val expected = new Path(strPath)
    val source = ConfigSource.string(s"""{ path: "$strPath" }""")
    source.at("path").load[Path].value shouldEqual expected
  }

  it should "be able to write correct path value to config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    val path = new Path(strPath)
    ConfigWriter[Path].to(path).unwrapped() shouldEqual strPath
  }

  it should "refuse an invalid path value" in {
    val source = ConfigSource.string(s"""{ path: "file:#//123" }""")
    source.at("path").load[Path] should failWithReason[CannotConvert]
  }

  it should "refuse an empty path value" in {
    val source = ConfigSource.string(s"""{ path: "" }""")
    source.at("path").load[Path] should failWithReason[EmptyStringFound]
  }
}
