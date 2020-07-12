package pureconfig

import pureconfig.DerivationChecks._
import pureconfig.generic.auto._

class DerivationSuite extends BaseSuite {

  class Custom(x: Int, s: String)
  class Custom2(x: Int, s: String)

  sealed trait Conf
  case class ConfA(a: Boolean, b: Option[Boolean]) extends Conf
  sealed trait ConfB extends Conf
  case class ConfB1(a: Int) extends ConfB
  case class ConfB2(a: String) extends ConfB
  case class ConfC(a: Option[Custom], b: Custom2) extends Conf

  case class RecConf1(a: RecConf2)
  case class RecConf2(a: RecConf1)

  case class RecFailConf1(a: RecFailConf2)
  case class RecFailConf2(a: RecFailConf1, b: Custom)

  val customReader: ConfigReader[Custom] = ConfigReader.fromFunction(_ => Right(new Custom(0, "")))
  val customReader2: ConfigReader[Custom2] = ConfigReader.fromFunction(_ => Right(new Custom2(0, "")))

  behavior of "Derivation"

  it should "always materialize an implicit when it can be found" in {
    implicitly[Derivation[ConfigReader[Int]]]
    implicitly[Derivation[ConfigReader[List[Int]]]]
    implicitly[Derivation[ConfigReader[ConfA]]]
    implicitly[Derivation[ConfigReader[ConfB]]]
    implicitly[Derivation[ConfigReader[RecConf1]]]
    implicitly[Derivation[ConfigReader[RecConf2]]]

    {
      implicit val cr = customReader
      implicit val cr2 = customReader2
      implicitly[Derivation[ConfigReader[Custom]]]
      implicitly[Derivation[ConfigReader[ConfC]]]
      implicitly[Derivation[ConfigReader[Conf]]]
    }

    implicitly[Derivation[ConfigWriter[ConfA]]]
    implicitly[Derivation[ConfigWriter[ConfB]]]
  }

  it should "fail with a message indicating the root reason when an implicit cannot be found" in {
    illTyped("implicitly[Derivation[ConfigReader[Custom]]]", "could not find a ConfigReader instance for type Custom")

    illTyped(
      "implicitly[Derivation[ConfigReader[ConfC]]]",
      "could not derive a ConfigReader instance for type ConfC, because:",
      "  - missing a ConfigReader instance for type Option\\[Custom\\], because:",
      "    - missing a ConfigReader instance for type Custom",
      "  - missing a ConfigReader instance for type Custom2"
    )

    illTyped(
      "implicitly[Derivation[ConfigReader[Conf]]]",
      "could not derive a ConfigReader instance for type Conf, because:",
      "  - missing a ConfigReader instance for type ConfC, because:",
      "    - missing a ConfigReader instance for type Option\\[Custom\\], because:",
      "      - missing a ConfigReader instance for type Custom",
      "    - missing a ConfigReader instance for type Custom2"
    )

    illTyped(
      "implicitly[Derivation[ConfigReader[RecFailConf1]]]",
      "could not derive a ConfigReader instance for type RecFailConf1, because:",
      "  - missing a ConfigReader instance for type RecFailConf2, because:",
      "    - missing a ConfigReader instance for type Custom"
    )

    illTyped(
      "implicitly[Derivation[ConfigReader[RecFailConf2]]]",
      "could not derive a ConfigReader instance for type RecFailConf2, because:",
      "  - missing a ConfigReader instance for type Custom"
    )

    // ---

    illTyped("implicitly[Derivation[ConfigWriter[Custom]]]", "could not find a ConfigWriter instance for type Custom")

    illTyped(
      "implicitly[Derivation[ConfigWriter[ConfC]]]",
      "could not derive a ConfigWriter instance for type ConfC, because:",
      "  - missing a ConfigWriter instance for type Option\\[Custom\\], because:",
      "    - missing a ConfigWriter instance for type Custom",
      "  - missing a ConfigWriter instance for type Custom2"
    )

    illTyped(
      "implicitly[Derivation[ConfigWriter[Conf]]]",
      "could not derive a ConfigWriter instance for type Conf, because:",
      "  - missing a ConfigWriter instance for type ConfC, because:",
      "    - missing a ConfigWriter instance for type Option\\[Custom\\], because:",
      "      - missing a ConfigWriter instance for type Custom",
      "    - missing a ConfigWriter instance for type Custom2"
    )

    // ---

    illTyped(
      "implicitly[Derivation[ConfigConvert[Custom]]]",
      "could not derive a ConfigConvert instance for type Custom, because:",
      "  - missing a ConfigReader instance for type Custom",
      "  - missing a ConfigWriter instance for type Custom"
    )

    illTyped(
      "implicitly[Derivation[ConfigConvert[Option[Custom]]]]",
      "could not derive a ConfigConvert instance for type Option\\[Custom\\], because:",
      "  - missing a ConfigReader instance for type Option\\[Custom\\], because:",
      "    - missing a ConfigReader instance for type Custom",
      "  - missing a ConfigWriter instance for type Option\\[Custom\\], because:",
      "    - missing a ConfigWriter instance for type Custom"
    )

    illTyped(
      "implicit val cr = customReader; implicitly[Derivation[ConfigConvert[Option[Custom]]]]",
      "could not derive a ConfigConvert instance for type Option\\[Custom\\], because:",
      "  - missing a ConfigWriter instance for type Option\\[Custom\\], because:",
      "    - missing a ConfigWriter instance for type Custom"
    )

    illTyped(
      "Derivation.materializeDerivation[ConfigReader[Conf]]",
      "could not derive a ConfigReader instance for type Conf, because:",
      "  - missing a ConfigReader instance for type ConfC, because:",
      "    - missing a ConfigReader instance for type Option\\[Custom\\], because:",
      "      - missing a ConfigReader instance for type Custom",
      "    - missing a ConfigReader instance for type Custom2"
    )
  }

  it should "fallback to a regular implicit search if it's not at the root of that search" in {
    illTyped(
      "implicitly[ConfigReader[Conf]]",
      "could not find implicit value for parameter e: pureconfig.ConfigReader\\[DerivationSuite.this.Conf\\]"
    )

    {
      implicit val cr = customReader
      implicit val cr2 = customReader2
      implicitly[ConfigReader[Conf]]
    }
  }
}
