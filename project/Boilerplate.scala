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

  val templates: Seq[Template] = Seq(
    GenProductReaders,
    GenProductWriters)

  val testTemplates: Seq[Template] = Seq(
    GenProductTests)

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

  /**
   * Return a sequence of the generated test files. As a side-effect, it actually generates them...
   */
  def genTests(dir: File): Seq[File] = testTemplates.map { template =>
    val tgtFile = template.filename(dir)
    IO.write(tgtFile, template.body)
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

  object GenProductWriters extends Template {
    def filename(root: sbt.File) = root / "pureconfig" / "ProductWriters.scala"

    def content(tv: TemplateVals): String = {
      import tv._

      val paramNames = synTypes.map(tpe => s"key$tpe: String").mkString(", ")
      val writerInstances = synTypes.map(tpe => s"writer$tpe: ConfigWriter[$tpe]").mkString(", ")

      val withValuesStr = if (arity != 1) {
        synTypes.zipWithIndex.map { case (tpe, i) =>
          s".withValue(key$tpe, writer$tpe.to(values._${i + 1}))"
        }.mkString
      } else
        s".withValue(key${synTypes.head}, writer${synTypes.head}.to(values))"

      block"""
        |package pureconfig
        |
        |import com.typesafe.config._
        |
        |private[pureconfig] trait ProductWriters {
        -
        -  final def forProduct$arity[B, ${`A..N`}]($paramNames)(f: B => ${if (arity != 1) s"Product$arity[${`A..N`}]" else s"${`A..N`}"})(implicit
        -    $writerInstances
        -  ): ConfigWriter[B] = new ConfigWriter[B] {
        -    def to(a: B): ConfigValue = {
        -      val values = f(a)
        -      val baseConf = ConfigFactory.empty()
        -      baseConf${withValuesStr}.root()
        -    }
        -  }
        |}
      """
    }
  }

  object GenProductTests extends Template {
    def filename(root: sbt.File) = root / "pureconfig" / "ForProductNSuite.scala"

    def content(tv: TemplateVals): String = {
      import tv._

      val params = (0 until arity).map(i => s"s$i: String").mkString(", ")
      val paramNames = (0 until arity).map(i => s""""s$i"""").mkString(", ")

      block"""
        |package pureconfig
        |
        |import org.scalacheck.ScalacheckShapeless._
        |
        |class ForProductNSuite extends BaseSuite {
        |
        |  behavior of "ConfigReader.forProductN and ConfigWriter.forProductN"
        -
        -  case class Foo$arity($params)
        -  object Foo$arity {
        -    implicit val foo${arity}Writer = ConfigWriter.forProduct$arity($paramNames)((Foo$arity.unapply _).andThen(_.get))
        -    implicit val foo${arity}Reader = ConfigReader.forProduct$arity($paramNames)(Foo$arity.apply)
        -  }
        -  checkArbitrary[Foo$arity]
        |}
      """
    }
  }
}
