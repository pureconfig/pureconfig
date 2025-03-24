package pureconfig.module

import scala.jdk.CollectionConverters._

import com.typesafe.config._
import io.circe._

import pureconfig.{ConfigReader, ConfigWriter}

package object circe {
  private def cvToJson(cv: ConfigValue): Json = {
    cv.valueType match {
      case ConfigValueType.NULL =>
        Json.Null
      case ConfigValueType.BOOLEAN =>
        Json.fromBoolean(cv.unwrapped.asInstanceOf[Boolean])
      case ConfigValueType.NUMBER =>
        cv.unwrapped match {
          case i: java.lang.Number if i.longValue() == i =>
            Json.fromLong(i.longValue())
          case d: java.lang.Number =>
            Json.fromDoubleOrNull(d.doubleValue())
        }
      case ConfigValueType.STRING =>
        Json.fromString(cv.unwrapped.asInstanceOf[String])
      case ConfigValueType.LIST => {
        val iter = cv.asInstanceOf[ConfigList].asScala.map(cvToJson _)
        Json.fromValues(iter)
      }
      case ConfigValueType.OBJECT => {
        val jsonMap = cv.asInstanceOf[ConfigObject].asScala.view.mapValues(cvToJson _).toMap
        val jsonObj = JsonObject.fromMap(jsonMap)
        Json.fromJsonObject(jsonObj)
      }
    }
  }

  private def jsonToCv(json: Json): ConfigValue = {
    json.fold(
      ConfigValueFactory.fromAnyRef(null),
      bool => ConfigValueFactory.fromAnyRef(bool),
      jnum =>
        jnum.toLong match {
          case Some(long) => ConfigValueFactory.fromAnyRef(long)
          case None => ConfigValueFactory.fromAnyRef(jnum.toDouble)
        },
      str => ConfigValueFactory.fromAnyRef(str),
      arr => ConfigValueFactory.fromIterable(arr.map(jsonToCv).asJava),
      obj => ConfigValueFactory.fromMap(obj.toMap.map { case (k, v) => k -> jsonToCv(v) }.asJava)
    )
  }

  implicit val circeJsonReader: ConfigReader[Json] =
    ConfigReader[ConfigValue].map(cvToJson)
  implicit val circeJsonWriter: ConfigWriter[Json] =
    ConfigWriter[ConfigValue].contramap(jsonToCv)
}
