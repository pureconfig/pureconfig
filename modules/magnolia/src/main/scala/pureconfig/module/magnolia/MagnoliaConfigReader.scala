package pureconfig.module.magnolia

import _root_.magnolia._
import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, UnknownKey, WrongSizeList }
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
    def from(cur: ConfigCursor): Result[A] = {
      cur.asObjectCursor.flatMap { objCur =>
        val res = ctx.constructMonadic[Result, Param[ConfigReader, A]#PType] { param =>
          val keyStr = hint.configKey(param.label)
          objCur.atKeyOrUndefined(keyStr) match {
            case keyCur if keyCur.isUndefined =>
              param.default match {
                case Some(defaultValue) if hint.useDefaultArgs => Right(defaultValue)
                case _ if param.typeclass.isInstanceOf[ReadsMissingKeys] => param.typeclass.from(keyCur)
                case _ => cur.failed(KeyNotFound.forKeys(keyStr, objCur.keys))
              }
            case keyCur =>
              param.typeclass.from(keyCur)
          }
        }
        if (hint.allowUnknownKeys || res.isLeft) res
        else {
          val usedKeys = ctx.parameters.map { param => hint.configKey(param.label) }.toSet
          val unknownKeyFailures = objCur.map.collect {
            case (k, keyCur) if !usedKeys(k) => keyCur.failureFor(UnknownKey(k))
          }.toList

          if (unknownKeyFailures.isEmpty) res
          else Left(ConfigReaderFailures(unknownKeyFailures.head, unknownKeyFailures.tail))
        }
      }
    }
  }

  private def combineTuple[A: ProductHint](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): Result[A] = {
      val collCur = cur.asListCursor.map(Right.apply).left.flatMap(failure =>
        cur.asObjectCursor.map(Left.apply).left.map(_ => failure))

      collCur.flatMap {
        case Left(objCur) => combineCaseClass(ctx).from(objCur)
        case Right(listCur) =>
          if (listCur.size != ctx.parameters.length) {
            cur.failed(WrongSizeList(ctx.parameters.length, listCur.size))
          } else {
            val fields = Result.sequence(ctx.parameters.zip(listCur.list).map {
              case (param, cur) => param.typeclass.from(cur)
            })
            fields.map(ctx.rawConstruct)
          }
      }
    }
  }

  private def combineValueClass[A](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): Result[A] =
      ctx.constructMonadic[Result, Param[ConfigReader, A]#PType](_.typeclass.from(cur))
  }

  def dispatch[A](ctx: SealedTrait[ConfigReader, A])(implicit hint: CoproductHint[A]): ConfigReader[A] = new ConfigReader[A] {
    def from(cur: ConfigCursor): Result[A] = {
      val res = ctx.subtypes.foldLeft(Right(None): Result[Option[A]]) {
        case (Right(None), subtype) =>
          hint.from(cur, subtype.typeName.short) match {
            case Right(Some(optCur)) =>
              subtype.typeclass.from(optCur).map(Some.apply) match {
                case Left(_) if hint.tryNextOnFail(subtype.typeName.short) => Right(None)
                case res => res
              }
            case res => res.asInstanceOf[Result[Option[A]]]
          }
        case (res, _) => res
      }
      res.flatMap {
        case Some(a) => Right(a)
        case None => Left(hint.noOptionFound(cur))
      }
    }
  }
}
