package pureconfig.module.magnolia

import _root_.magnolia._
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, WrongSizeList }
import pureconfig.generic.CoproductHint.{ Attempt, Skip, Use }
import pureconfig.generic.ProductHint.FieldHint
import pureconfig.generic.{ CoproductHint, ProductHint }

/**
 * An object containing Magnolia `combine` and `dispatch` methods to generate `ConfigReader` instances.
 */
object MagnoliaConfigReader {

  def combine[A](ctx: CaseClass[ConfigReader, A])(implicit hint: ProductHint[A]): ConfigReader[A] =
    if (ctx.typeName.full.startsWith("scala.Tuple")) combineTuple(ctx)
    else if (ctx.isValueClass) combineValueClass(ctx)
    else combineCaseClass(ctx)

  private def combineCaseClass[A](ctx: CaseClass[ConfigReader, A])(implicit hint: ProductHint[A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): ConfigReader.Result[A] = {
      cur.asObjectCursor.flatMap { objCur =>
        val hints = ctx.parameters.map { param => param.label -> hint.from(objCur, param.label) }.toMap

        val res = ctx.constructEither[ConfigReaderFailures, Param[ConfigReader, A]#PType] { param =>
          val fieldHint = hints(param.label)
          lazy val reader = param.typeclass
          lazy val keyNotFoundFailure = cur.failed(KeyNotFound.forKeys(fieldHint.field, objCur.keys))
          (fieldHint, param.default) match {
            case (FieldHint(cursor, _, _, true), Some(defaultValue)) if cursor.isUndefined =>
              Right(defaultValue)
            case (FieldHint(cursor, _, _, _), _) if reader.isInstanceOf[ReadsMissingKeys] || !cursor.isUndefined =>
              reader.from(cursor)
            case _ =>
              keyNotFoundFailure
          }
        }.left.map(_.reduce(_ ++ _))

        val filteredObj = hints.foldLeft(objCur) {
          case (currObj, (_, FieldHint(_, field, true, _))) =>
            currObj.withoutKey(field)
          case (currObj, _) =>
            currObj
        }

        hint.bottom(filteredObj).fold(
          res)(
          bottomFailures =>
            res match {
              case Left(failures) => Left(failures ++ bottomFailures)
              case _ => Left(bottomFailures)
            })
      }
    }
  }

  private def combineTuple[A: ProductHint](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): ConfigReader.Result[A] = {
      val collCur = cur.asListCursor.map(Right.apply).left.flatMap(failure =>
        cur.asObjectCursor.map(Left.apply).left.map(_ => failure))

      collCur.flatMap {
        case Left(objCur) => combineCaseClass(ctx).from(objCur)
        case Right(listCur) =>
          if (listCur.size != ctx.parameters.length) {
            cur.failed(WrongSizeList(ctx.parameters.length, listCur.size))
          } else {
            val fields = ConfigReader.Result.sequence(ctx.parameters.zip(listCur.list).map {
              case (param, cur) => param.typeclass.from(cur)
            })
            fields.map(ctx.rawConstruct)
          }
      }
    }
  }

  private def combineValueClass[A](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): ConfigReader.Result[A] =
      ctx.constructMonadic[ConfigReader.Result, Param[ConfigReader, A]#PType](_.typeclass.from(cur))
  }

  def dispatch[A](ctx: SealedTrait[ConfigReader, A])(implicit hint: CoproductHint[A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): ConfigReader.Result[A] = {
      val (_, res) = ctx.subtypes.foldLeft[(Boolean, Either[List[(String, ConfigReaderFailures)], A])]((true, Left(Nil))) {
        case (acc, subtype) =>
          acc match {
            case (false, _) => acc
            case (true, Right(_)) => acc
            case (true, Left(attempts)) =>
              val typeName = subtype.typeName.short
              hint.from(cur, typeName) match {
                case Use(cur) =>
                  subtype.typeclass.from(cur).fold(
                    failures => (false, Left(attempts :+ (typeName -> failures))),
                    res => (false, Right(res)))
                case Attempt(cur) =>
                  subtype.typeclass.from(cur).fold(
                    failures => (true, Left(attempts :+ (typeName -> failures))),
                    res => (false, Right(res)))
                case Skip => acc
              }
          }
      }
      res.left.map(hint.bottom(cur, _))
    }
  }
}
