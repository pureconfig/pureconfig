package pureconfig.module.magnolia

import _root_.magnolia._
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, WrongSizeList }
import pureconfig.generic.ProductHint.UseOrDefault
import pureconfig.generic.error.MissingCoproductChoice
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
          (fieldHint, param.default) match {
            case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
              Right(defaultValue)
            case (action, _) if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined =>
              reader.from(action.cursor)
            case _ =>
              cur.failed(KeyNotFound.forKeys(fieldHint.field, objCur.keys))
          }
        }.left.map(_.reduce(_ ++ _))

        val usedFields = actions.map(_._2.field).toSet
        ConfigReader.Result.zipWith(res, hint.bottom(objCur, usedFields).toLeft(()))((r, _) => r)
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
      def readerFor(option: String) =
        ctx.subtypes.find(_.typeName.short == option).map(_.typeclass)

      hint.from(cur, ctx.subtypes.map(_.typeName.short)).right.flatMap {
        case CoproductHint.Use(cur, option) =>
          readerFor(option) match {
            case Some(value) => value.from(cur)
            case None => ConfigReader.Result.fail[A](cur.failureFor(MissingCoproductChoice(option)))
          }

        case CoproductHint.Attempt(cur, options, combineF) =>
          val initial: Either[List[(String, ConfigReaderFailures)], A] = Left(List.empty)
          val res = options.foldLeft(initial) { (curr, option) =>
            curr.left.flatMap { currentFailures =>
              readerFor(option) match {
                case Some(value) => value.from(cur).left.map(f => currentFailures :+ (option -> f))
                case None => Left(currentFailures :+ (option -> ConfigReaderFailures(cur.failureFor(MissingCoproductChoice(option)))))
              }
            }
          }
          res.left.map(combineF)
      }
    }
  }
}
