package pureconfig.module.hadoop

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import org.apache.hadoop.fs.Path

import pureconfig.error.{CannotConvert, EmptyStringFound}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigWriter}

class HadoopModuleTest extends BaseSuite {

  behavior of "HDFS Path ConfigConvert"

  it should "be able to read correct path value from config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    configValue(s""""$strPath"""").to[Path].value shouldEqual new Path(strPath)
  }

  it should "be able to write correct path value to config" in {
    val strPath = "hdfs://my.domain/foo/bar/baz.gz"
    val path = new Path(strPath)
    ConfigWriter[Path].to(path).unwrapped() shouldEqual strPath
  }

  it should "refuse an invalid path value" in {
    configValue(""""file:#//123"""").to[Path] should failWithReason[CannotConvert]
  }

  it should "refuse an empty path value" in {
    configValue(s"""""""").to[Path] should failWithReason[EmptyStringFound]
  }
}
