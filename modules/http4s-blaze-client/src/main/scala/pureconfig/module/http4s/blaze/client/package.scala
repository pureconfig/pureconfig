package pureconfig.module.http4s.blaze

import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}
import cats.implicits._
import cats.data.NonEmptyChain

package object client {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(
      str =>
        Uri
          .fromString(str)
          .fold(
            err => Left(CannotConvert(str, "Uri", err.sanitized)),
            uri => Right(uri)
        )
    )

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.renderString)

  implicit def blazeClientBuilderReader[F[_]]
    : ConfigReader[BlazeClientBuilder[F]] =
    ConfigReader.fromCursor[BlazeClientBuilder[F]] { cur =>
      cur.asObjectCursor.flatMap { objCur =>
        val x = objCur.map
          .map {
            case ("bufferSize", crs) =>
              for {
                bufferSize <- crs.asInt
              } yield
                ((b: BlazeClientBuilder[F]) => b.withBufferSize(bufferSize))
          }
          .map {
            _.leftMap(crfs => NonEmptyChain(crfs.head, crfs.tail: _*)).toValidated
          }
          .toList
        val y = x.sequence
        ???
      }
//      for {
//        objCur <- cur.asObjectCursor // 1
//        nameCur <- objCur.map.map {
//          case ("bufferSize", crs) =>
//            for {
//              bufferSize <- crs.asInt
//            } yield ((b: BlazeClientBuilder[F]) => b.withBufferSize(bufferSize))
//        } // 2
//        name <- nameCur.asString // 3
//      } yield new Person(firstNameOf(name), lastNamesOf(name))
    }

}
