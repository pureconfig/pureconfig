package pureconfig.generic.error

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import pureconfig.BaseSuite
import pureconfig.error.{ConfigReaderFailures, ConvertFailure, KeyNotFound, UnknownKey}

class GenericFailuresSuite extends BaseSuite {

  behavior of "generic failures"

  it should "print a message displaying relevant errors for coproduct derivation" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(
        UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("unexpected")),
        None,
        "values.v1.type"
      ),
      ConvertFailure(KeyNotFound("type", Set()), None, "values.v3")
    )

    failures.prettyPrint() shouldBe
      s"""|at 'values.v1.type':
          |  - Unexpected value "unexpected" found. Note that the default transformation for representing class names in config values changed from converting to lower case to converting to kebab case in version 0.11.0 of PureConfig. See https://pureconfig.github.io/docs/overriding-behavior-for-sealed-families.html for more details on how to use a different transformation.
          |at 'values.v3':
          |  - Key not found: 'type'.""".stripMargin
  }

  it should "print a message showing the errors of the attempted options when no valid coproduct choice was found" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(
        NoValidCoproductOptionFound(
          ConfigFactory.parseString("""{"a":{"C":4}}""").root(),
          Seq(
            "Option1" -> ConfigReaderFailures(
              ConvertFailure(KeyNotFound("b", Set()), None, "a"),
              ConvertFailure(UnknownKey("c"), None, "a.C")
            ),
            "Option2" -> ConfigReaderFailures(ConvertFailure(KeyNotFound("c", Set("C")), None, "a"))
          )
        ),
        None,
        "a"
      )
    )

    failures.prettyPrint() shouldBe
      s"""|at 'a':
          |  - No valid coproduct option found for '{"a":{"C":4}}'.
          |    Can't use coproduct option 'Option1':
          |      at 'a':
          |        - Key not found: 'b'.
          |      at 'a.C':
          |        - Unknown key.
          |    Can't use coproduct option 'Option2':
          |      at 'a':
          |        - Key not found: 'c'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |            - 'C'""".stripMargin
  }
}
