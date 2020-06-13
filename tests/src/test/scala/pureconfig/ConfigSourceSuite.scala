package pureconfig

import java.io.IOException

import com.typesafe.config.{ Config, ConfigFactory, ConfigValueType }
import pureconfig.PathUtils._
import pureconfig.error._
import pureconfig.generic.auto._

class ConfigSourceSuite extends BaseSuite {

  behavior of "ConfigSource"

  it should "load from application.conf and reference.conf by default" in {
    case class Conf(d: Double, i: Int, s: String)
    ConfigSource.default.load[Conf] shouldBe Right(Conf(0D, 0, "app_value"))
  }

  it should "allow reading at a namespace" in {
    case class Conf(f: Float)
    ConfigSource.default.at("foo").load[Conf] shouldBe Right(Conf(3.0F))
  }

  it should "allow reading from a config object" in {
    case class Conf(d: Double, i: Int)
    val conf = ConfigFactory.parseString("{ d: 0.5, i: 10 }")
    ConfigSource.fromConfig(conf).load[Conf] shouldBe Right(Conf(0.5D, 10))
  }

  it should "allow reading from a config object at a namespace" in {
    case class Conf(f: Float)
    val conf = ConfigFactory.parseString("foo.bar { f: 1.0 }")
    ConfigSource.fromConfig(conf).at("foo.bar").load[Conf] shouldBe Right(Conf(1.0F))
    ConfigSource.fromConfig(conf).at("bar.foo").load[Conf] should failWith(KeyNotFound("bar", Set.empty), "", stringConfigOrigin(1))
  }

  it should "allow reading scalar values from a config object at a namespace" in {
    val conf = ConfigFactory.parseString("foo { bar { f: 1.0 }, baz: 3.4 }")
    val source = ConfigSource.fromConfig(conf)

    source.at("foo.bar.f").load[Float] shouldBe Right(1.0F)
    source.at("foo.bar.h").load[Float] should failWith(KeyNotFound("h", Set.empty), "foo.bar", stringConfigOrigin(1))
    source.at("bar.foo.f").load[Float] should failWith(KeyNotFound("bar", Set.empty), "", stringConfigOrigin(1))
    source.at("foo.baz.f").load[Float] should failWith(
      WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), "foo.baz", stringConfigOrigin(1))

    source.at("foo.bar.f").load[Option[Float]] shouldBe Right(Some(1.0F))
    source.at("foo.bar.h").load[Option[Float]] should failWith(KeyNotFound("h", Set.empty), "foo.bar", stringConfigOrigin(1))
    source.at("bar.foo.f").load[Option[Float]] should failWith(KeyNotFound("bar", Set.empty), "", stringConfigOrigin(1))
    source.at("foo.baz.f").load[Option[Float]] should failWith(
      WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), "foo.baz", stringConfigOrigin(1))
    source.at("foo").at("baz").at("f").load[Option[Float]] should failWith(
      WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), "foo.baz", stringConfigOrigin(1))
  }

  it should "handle correctly namespaces with special chars" in {
    val conf = ConfigFactory.parseString(""" "fo.o" { "ba r" { f: 1.0 }, "ba z": 3.4 }""")
    val source = ConfigSource.fromConfig(conf)

    source.at("\"fo.o\".\"ba r\".f").load[Float] shouldBe Right(1.0F)

    source.at("\"fo.o\".\"ba r\".h").load[Float] should failWith(
      KeyNotFound("h", Set.empty), "\"fo.o\".\"ba r\"", stringConfigOrigin(1))

    source.at("\"fo.o\".\"ba z\".h").load[Float] should failWith(
      WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), "\"fo.o\".\"ba z\"", stringConfigOrigin(1))

    source.at("\"b.a.r\".foo.f").load[Float] should failWith(
      KeyNotFound("b.a.r", Set.empty), "", stringConfigOrigin(1))
  }

  it should "allow reading from a configuration file" in {
    case class Conf(s: String, b: Boolean)
    val path = createTempFile("""{ b: true, s: "str" }""")
    ConfigSource.file(path).load[Conf] shouldBe Right(Conf("str", true))
    ConfigSource.file(nonExistingPath).load[Conf] should failLike {
      case CannotReadFile(path, _) => be(path)(nonExistingPath)
    }
  }

  it should "allow reading from a configuration file at a namespace" in {
    case class Conf(s: String, b: Boolean)
    val path = createTempFile("""foo.bar { b: true, s: "str" }""")
    ConfigSource.file(path).at("foo.bar").load[Conf] shouldBe Right(Conf("str", true))
    ConfigSource.file(nonExistingPath).at("foo.bar").load[Conf] should failLike {
      case CannotReadFile(path, _) => be(path)(nonExistingPath)
    }
    ConfigSource.file(path).at("bar.foo").load[Conf] should failWith(KeyNotFound("bar", Set.empty))
  }

  it should "allow reading from a URL" in {
    case class Conf(s: String, b: Boolean)
    val path = createTempFile("""{ b: true, s: "str" }""")
    ConfigSource.url(path.toUri.toURL).load[Conf] shouldBe Right(Conf("str", true))
    ConfigSource.url(nonExistingPath.toUri.toURL).load[Conf] should failLike {
      case CannotReadUrl(url, _) => be(url)(nonExistingPath.toUri.toURL)
    }
  }

  it should "allow reading from a string" in {
    case class Conf(s: String, b: Boolean)
    val confStr = """{ b: true, s: "str" }"""
    ConfigSource.string(confStr).load[Conf] shouldBe Right(Conf("str", true))
  }

  case class MyService(host: String, port: Int, useHttps: Boolean)

  it should "allow reading from resources" in {
    ConfigSource.resources("conf/configSource/full.conf").at("my-service").load[MyService] shouldBe
      Right(MyService("example.com", 8080, true))

    ConfigSource.resources("nonExistingResource").load[MyService] should failLike {
      case CannotReadResource("nonExistingResource", Some(reason)) => be(an[IOException])(reason)
    }
  }

  it should "allow chaining required fallback sources" in {
    val defaults = ConfigSource.resources("conf/configSource/defaults.conf")
    val overrides = ConfigSource.resources("conf/configSource/overrides.conf")
    val main = ConfigSource.resources("conf/configSource/main.conf")

    overrides.
      withFallback(main).
      withFallback(defaults).
      at("my-service").load[MyService] shouldBe Right(MyService("example.com", 8081, true))

    val nonExisting1 = ConfigSource.resources("nonExistingResource1")
    val nonExisting2 = ConfigSource.resources("nonExistingResource2")

    overrides.
      withFallback(nonExisting1).
      withFallback(main).
      withFallback(defaults).
      at("my-service").load[MyService] should matchPattern {
        case Left(ConfigReaderFailures(CannotReadResource("nonExistingResource1", _))) =>
      }

    overrides.
      withFallback(nonExisting1).
      withFallback(main).
      withFallback(nonExisting2).
      withFallback(defaults).
      at("my-service").load[MyService].left.map(_.toList) should matchPattern {
        case Left(List(
          CannotReadResource("nonExistingResource1", _),
          CannotReadResource("nonExistingResource2", _))) =>
      }
  }

  it should "allow chaining optional fallback sources" in {
    val defaults = ConfigSource.resources("conf/configSource/defaults.conf")
    val overrides = ConfigSource.resources("conf/configSource/overrides.conf")
    val main = ConfigSource.resources("conf/configSource/main.conf")

    overrides.optional.
      withFallback(main.optional).
      withFallback(defaults.optional).
      at("my-service").load[MyService] shouldBe Right(MyService("example.com", 8081, true))

    val nonExisting1 = ConfigSource.resources("nonExistingResource1")
    val nonExisting2 = ConfigSource.resources("nonExistingResource2")

    overrides.optional.
      withFallback(nonExisting1.optional).
      withFallback(main.optional).
      withFallback(defaults.optional).
      at("my-service").load[MyService] shouldBe Right(MyService("example.com", 8081, true))

    overrides.optional.
      withFallback(nonExisting1.optional).
      withFallback(main.optional).
      withFallback(nonExisting2.optional).
      withFallback(defaults.optional).
      at("my-service").load[MyService] shouldBe Right(MyService("example.com", 8081, true))
  }

  it should "fail when an optional source exists but has syntax errors" in {
    ConfigSource.file("non-existing-file.conf").optional.load[Config] shouldBe Right(ConfigFactory.empty)
    ConfigSource.string("4}{2").optional.load[Config] should failWithType[CannotParse]
  }

  it should "allow recovering a failed source with another one" in {
    val appSource = ConfigSource.file("non-existing-file.conf")
    val otherSource = ConfigSource.string("{ name = John, age = 33 }")

    case class Conf(name: String, age: Int)

    appSource.recoverWith { case ConfigReaderFailures(_: CannotRead) => otherSource }.load[Conf] shouldBe
      Right(Conf("John", 33))
    appSource.recoverWith { case ConfigReaderFailures(_: CannotParse) => otherSource }.load[Conf] should
      failWithType[CannotReadFile]
  }

  it should "defer config resolution until all fallbacks are merged" in {
    case class Conf(host: String)
    val resolve1 = ConfigSource.resources("conf/configSource/resolve1.conf")
    val resolve2 = ConfigSource.resources("conf/configSource/resolve2.conf")

    resolve1.withFallback(resolve2).load[Conf] shouldBe Right(Conf("myhost1.example.com"))
  }

  it should "fail safely when a substitution can't be resolved" in {
    case class Conf(hostOverride: String)
    val resolve1 = ConfigSource.resources("conf/configSource/resolve1.conf")

    resolve1.value() should failLike {
      case CannotParse(msg, _) => be(msg)(s"Could not resolve substitution to a value: $${host-suffix}")
    }

    resolve1.load[Conf] should failLike {
      case CannotParse(msg, _) => be(msg)(s"Could not resolve substitution to a value: $${host-suffix}")
    }
  }

  it should "define an empty source" in {
    ConfigSource.empty.load[Config] shouldBe Right(ConfigFactory.empty)
  }
}
