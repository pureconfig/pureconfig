package pureconfig
package generic

import scala.compiletime.{constValue, constValueTuple, erasedValue, summonInline}
import scala.compiletime.ops.int.*
import scala.deriving.Mirror

import pureconfig.error.{
  CannotConvert,
  ConfigReaderFailures,
  ConvertFailure,
  KeyNotFound,
  UnknownKey,
  WrongSizeList
}

export Product.deriveForMirroredProduct

object Product:
  inline def deriveForMirroredProduct[A](using m: Mirror.ProductOf[A]): ConfigReader[A] =
    val hint = summonInline[ProductHint[A]]

    new ConfigReader[A]:
      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        for
          objCur <- cur.asObjectCursor
          result <-
            val results =
              for
                (field, reader) <- summonFor[A]
              yield
                val action = hint.from(objCur, field)
                if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined then
                  reader.from(action.cursor)
                else
                  cur.failed(KeyNotFound.forKeys(action.field, objCur.keys))

            ConfigReader.Result.sequence(results)
              .map(
                _.reverse
                  .foldLeft[Tuple](EmptyTuple)((t, v) => v *: t)
                  .asInstanceOf[m.MirroredElemTypes]
              )
        yield m.fromProduct(result)

  inline def summonFor[A](using m: Mirror.ProductOf[A]): List[(String, ConfigReader[?])] =
    labelsFor[m.MirroredElemLabels].zip(summonConfigReaders[m.MirroredElemTypes])

  inline def summonConfigReaders[T <: Tuple]: List[ConfigReader[?]] =
    inline erasedValue[T] match
      case _: (h *: t) =>
        inline erasedValue[h] match
          case _: Tuple => deriveForTuple[h & Tuple] :: summonConfigReaders[t]
          case _ => summonInline[ConfigReader[h]] :: summonConfigReaders[t]

          
      case _: EmptyTuple => Nil

  inline def deriveForTuple[T <: Tuple]: ConfigReader[T] =
    new ConfigReader[T]:
      def from(cur: ConfigCursor): ConfigReader.Result[T] =
        for
          listCur <- asList(cur)
          result <- read[T, 0](listCur.list)
        yield result

      def asList(cur: ConfigCursor) =
        cur.asListCursor.flatMap { listCur =>
          if constValue[Tuple.Size[T]] == listCur.size then
            Right(listCur)
          else
            listCur.failed(
              WrongSizeList(constValue[Tuple.Size[T]], listCur.size)
            )
        }

  inline def read[T <: Tuple, N <: Int](cursors: List[ConfigCursor]): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match
      case _: (h *: t) =>
        val h = summonInline[ConfigReader[h]].from(cursors(constValue[N]))

        h -> read[t, N + 1](cursors) match
          case (Right(h), Right(t)) => Right((h *: t).asInstanceOf[T])
          case (Left(h), Left(t))   => Left(h ++ t)
          case (_, Left(failures))  => Left(failures)
          case (Left(failures), _)  => Left(failures)

      case _: EmptyTuple =>
        Right(EmptyTuple.asInstanceOf[T])
end Product
