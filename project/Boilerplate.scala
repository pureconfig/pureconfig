import sbt._

/**
 * Generate a range of boilerplate classes that would be tedious to craft by hand.
 *
 * Copied, with modifications, from
 * [[https://github.com/milessabin/shapeless/blob/master/project/Boilerplate.scala Shapeless]].
 *
 * @author Miles Sabin
 * @author Kevin Wright
 */
object Boilerplate {
  import scala.StringContext._

  implicit class BlockHelper(val sc: StringContext) extends AnyVal {
    def block(args: Any*): String = {
      val interpolated = sc.standardInterpolator(treatEscapes, args)
      val rawLines = interpolated split '\n'
      val trimmedLines = rawLines map { _ dropWhile (_.isWhitespace) }
      trimmedLines mkString "\n"
    }
  }

  val templates: Seq[Template] = Seq(GenProductReaders)

  val header = "// auto-generated boilerplate"
  val maxArity = 22

  /**
   * Returns a seq of the generated files. As a side-effect, it actually generates them...
   */
  def gen(dir : File) = for (t <- templates) yield {
    val tgtFile = t.filename(dir)
    IO.write(tgtFile, t.body)
    tgtFile
  }

  class TemplateVals(val arity: Int) {
    val synTypes = (0 until arity).map(n => s"A$n")
    val synVals = (0 until arity).map(n => s"a$n")

    val `A..N` = synTypes.mkString(", ")
  }

  /**
   * Blocks in the templates below use a custom interpolator, combined with post-processing to
   * produce the body.
   *
   * - The contents of the `header` val is output first
   * - Then the first block of lines beginning with '|'
   * - Then the block of lines beginning with '-' is replicated once for each arity,
   *   with the `templateVals` already pre-populated with relevant relevant vals for that arity
   * - Then the last block of lines prefixed with '|'
   *
   * The block otherwise behaves as a standard interpolated string with regards to variable
   * substitution.
   */
  trait Template {
    def filename(root: File): File
    def content(tv: TemplateVals): String
    def range: IndexedSeq[Int] = 1 to maxArity
    def body: String = {
      val headerLines = header.split('\n')
      val raw = range.map(n => content(new TemplateVals(n)).split('\n').filterNot(_.isEmpty))
      val preBody = raw.head.takeWhile(_.startsWith("|")).map(_.tail)
      val instances = raw.flatMap(_.filter(_.startsWith("-")).map(_.tail))
      val postBody = raw.head.dropWhile(_.startsWith("|")).dropWhile(_.startsWith("-")).map(_.tail)
      (headerLines ++ preBody ++ instances ++ postBody).mkString("\n")
    }
  }

  object GenProductReaders extends Template {
    def filename(root: sbt.File) = root / "pureconfig" / "ProductReaders.scala"

    def content(tv: TemplateVals): String = {
      import tv._

      val paramNames = synTypes.map(tpe => s"key$tpe: String").mkString(", ")
      val readerInstances = synTypes.map(tpe => s"reader$tpe: ConfigReader[$tpe]").mkString(", ")

      val results = synTypes.zip(synVals).map { case (tpe, v) =>
        s"""
           |-        val ${v}Result = objCur.atKeyOrUndefined(key$tpe) match {
           |-          case cur if cur.isUndefined =>
           |-            if (reader$tpe.isInstanceOf[AllowMissingKey])
           |-              reader$tpe.from(cur)
           |-            else
           |-              objCur.failed(KeyNotFound.forKeys(key$tpe, objCur.keys))
           |-          case cur => reader$tpe.from(cur)
           |-        }
           |-
         """.stripMargin
      }.mkString("\n")

      val curriedTypes = synTypes.mkString(" => ")

      val resultCombination = synVals.foldLeft("baseF") { case (curr, v) =>
        s"ConvertHelpers.combineResults($curr, ${v}Result)(_(_))"
      }

      block"""
        |package pureconfig
        |
        |import pureconfig.error._
        |
        |private[pureconfig] trait ProductReaders {
        -
        -  final def forProduct$arity[B, ${`A..N`}]($paramNames)(f: (${`A..N`}) => B)(implicit
        -    $readerInstances
        -  ): ConfigReader[B] = new ConfigReader[B] {
        -    def from(cur: ConfigCursor): Either[ConfigReaderFailures, B] =
        -      cur.asObjectCursor.right.flatMap { objCur =>
                 $results
        -        val baseF: Either[ConfigReaderFailures, $curriedTypes => B] = Right(${if (arity != 1) "f.curried" else "f"})
        -        $resultCombination
        -      }
        -  }
        |}
      """
    }
  }
}
