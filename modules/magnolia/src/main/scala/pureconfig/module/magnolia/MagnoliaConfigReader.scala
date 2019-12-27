package pureconfig.module.magnolia

import _root_.magnolia._
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, WrongSizeList }
import pureconfig.generic.CoproductHint.{ Attempt, Skip, Use }
import pureconfig.generic.ProductHint.UseOrDefault
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
        val actions = ctx.parameters.map { param => param.label -> hint.from(objCur, param.label) }.toMap

        val res = ctx.constructEither[ConfigReaderFailures, Param[ConfigReader, A]#PType] { param =>
          val fieldHint = actions(param.label)
          lazy val reader = param.typeclass
          lazy val keyNotFoundFailure = cur.failed(KeyNotFound.forKeys(fieldHint.field, objCur.keys))
          (fieldHint, param.default) match {
            case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
              Right(defaultValue)
            case (action, _) if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined =>
              reader.from(action.cursor)
            case _ =>
              keyNotFoundFailure
          }
        }.left.map(_.reduce(_ ++ _))

        val usedFields = actions.map(_._2.field).toSet
        hint.bottom(objCur, usedFields).fold(
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
      val (_, res) = ctx.subtypes.foldLeft[(Boolean, Either[List[(String, ConfigReaderFailures)], ConfigReader.Result[A]])]((true, Left(Nil))) {
        case (acc, subtype) =>
          acc match {
            case (false, _) => acc
            case (true, Right(_)) => acc
            case (true, Left(attempts)) =>
              val typeName = subtype.typeName.short
              hint.from(cur, typeName) match {
                case Right(Use(cur)) =>
                  subtype.typeclass.from(cur).fold(
                    failures => (false, Left(attempts :+ (typeName -> failures))),
                    res => (false, Right(Right(res))))
                case Right(Attempt(cur)) =>
                  subtype.typeclass.from(cur).fold(
                    failures => (true, Left(attempts :+ (typeName -> failures))),
                    res => (false, Right(Right(res))))
                case Right(Skip) => acc
                case l @ Left(_) => (false, Right(l.asInstanceOf[ConfigReader.Result[A]]))
              }
          }
      }
      res.left.map(hint.bottom(cur, _)).right.flatMap(identity)
    }
  }
}
