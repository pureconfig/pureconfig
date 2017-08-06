### Add support for complex types

Support for more complex types, such as Java configurations, can be achieved with many different methods,
each one with pros and cons. The preferred method is shown [here](#method-1-use-a-scala-copy-of-the-configuration)
but for the sake of completeness this section will describe all the methods and provide hints on when one should be used instead of others. This
section is organized in the following way: the [first part](#data-type-to-support) describes an example of complex type to
support, the [second part](#add-support-for-identifiable) provides the three methods to add support for that complex type and the
[final part](#summary) discusses when one method should be picked instead of another one.

#### Data type to support

First of all, let's start with the data type that we want to support. 
The example is composed by one interface and two implementations:

```scala
trait Identifiable {
  def getId: String
}

class Class1(id: String) extends Identifiable {
  def getId: String = id
}

class Class2(id: String, value: Int) extends Identifiable {
  def getId: String = id
  def getValue: Int = value
}
```

Because PureConfig is based on Shapeless and Shapeless doesn't support non-case classes and interfaces,
the class above cannot be read automatically by PureConfig.

#### Add support for Identifiable

##### Method 1: Use a Scala copy of the configuration

Because  PureConfig works out-of-the-box with Scala sealed familes of case classes, one simple solution
to support unsupported complex types is to

1. define a hierarchy of Scala classes mirroring the complex type;
2. define a conversion from this Scala configuration to the complex type.

In the case above, we could do:

```scala
import pureconfig._
import pureconfig.error._

// define the "dummy" scala hierarchy
sealed trait IdentifiableDummy {
  def id: String
  def toIdentifiable: Identifiable
}

final case class Class1Dummy(id: String) extends IdentifiableDummy {
  def toIdentifiable: Identifiable = new Class1(id)
}

final case class Class2Dummy(id: String, value: Int) extends IdentifiableDummy {
  def toIdentifiable: Identifiable = new Class2(id, value) 
}

// change the coproduct hint to tell PureConfig that the type value specified
// is the name of the class - "Dummy" because we want the type to be the one
// of the new types, not in the mirroring types
implicit val identifiableCoproductHint = new FieldCoproductHint[IdentifiableDummy]("type") {
  override protected def fieldValue(name: String): String =
     name.take(name.length - "Dummy".length).toLowerCase
}

// we tell PureConfig that to read Identifiable, it has to read IdentifiableDummy first
// and then maps it to Identifiable
implicit val identifiableConfigReader: ConfigReader[Identifiable] =
  ConfigReader[IdentifiableDummy].map(_.toIdentifiable)
```

**pros**: it's idiomatic and elegant. It uses a type supported by PureConfig to extract the data
needed from the configuration and then map it to the original complex type. Note that this
method follows what you usually do in other libraries (e.g. Circe) to support new data types and
it's both composable and nice to read.

**cons**: needs the configuration to be duplicated and PureConfig needs to do two
steps to read it.

##### Method 2: Implement `ConfigReader[Identifiable]` manually

Similarly to adding support for simple types, it is possible to manually create a
`ConfigReader[Identifiable]`:

```scala
import com.typesafe.config._
import pureconfig._
import pureconfig.error._

def extractId(obj: ConfigObject): Either[ConfigReaderFailures, String] =
  Option(obj.get("id")) match {
    case None =>
      Left(ConfigReaderFailures(KeyNotFound("id", ConfigValueLocation(obj))))
    case Some(id) =>
      ConfigReader[String].from(id)
  }

def extractValue(obj: ConfigObject): Either[ConfigReaderFailures, Int] =
  Option(obj.get("value")) match {
    case None =>
      Left(ConfigReaderFailures(KeyNotFound("value", ConfigValueLocation(obj))))
    case Some(value) =>
      ConfigReader[Int].from(value)
  }

implicit val identifiableConfigReader = ConfigReader.fromFunction({
case obj: ConfigObject =>
 Option(obj.get("type")) match {
   case None =>
     Left(ConfigReaderFailures(KeyNotFound("type", ConfigValueLocation(obj))))
   case Some(t) if t.valueType() == ConfigValueType.STRING =>
     t.unwrapped().asInstanceOf[String] match {
       case "class1" => extractId(obj).right.map(new Class1(_))
       case "class2" =>
         for {
           id <- extractId(obj).right
           value <- extractValue(obj).right
         } yield new Class2(id, value)
       case x =>
         Left(ConfigReaderFailures(CannotConvert(obj.toString,
           "Identifiable", s"type has value $t instead of class1 or class2",
           ConfigValueLocation(obj),
           "")))
     }
   case Some(t) =>
     Left(ConfigReaderFailures(CannotConvert(obj.toString,
       "Identifiable", s"type has value $t instead of class1 or class2",
       ConfigValueLocation(obj),
       "")))
 }
case wrong =>
  Left(ConfigReaderFailures(WrongType(wrong.valueType(), Set(ConfigValueType.OBJECT), ConfigValueLocation(wrong), "")))
})
```

**pros**: The main feature of this method is that PureConfig delegates to it when decoding your configuration.
PureConfig takes the manually defined `ConfigReader[Identifiable]` as a blackbox and applies it when it has to read your configuration.

**cons**: The main feature of this method is also its drawback, because by taking control over 
PureConfig in how to read sealed families of case classes, it prevents PureConfig from doing its 
magic. For instance, it prevents PureConfig from using the `CoproductHint` as strategy to read 
`Identifiable`. This is, generally speaking, not a good idea. In most cases you want PureConfig 
features also for your custom class.

##### Method 3: Give a Generic representation of your configuration

PureConfig is based on shapeless Generics which are used to represent data structures in a
generic form. The reason why in method 2 PureConfig was able to decode
`IdentifiableDummy` but not `Identifiable` prior our code is that `IdentifiableDummy`
has a `Generic` instance provided by shapeless while `Identifiable` doesn't.
This method consists of adding the generic representation for the configuration.

Let's start with the concrete classes `Class1` and `Class2`. PureConfig needs to know
two things in order to read them:

1. Their generic representation;
2. For each field in the generic representation, whether each field in the generic representation has a default value.

Point 1 is solved by the code:

```scala
import shapeless._
import shapeless.labelled._

// create the singleton idw for the field id
val idw = Witness('id) 

implicit val class1Generic = new LabelledGeneric[Class1] {
  // the generic representation of Class1 is the field id of type String
  override type Repr = FieldType[idw.T, String] :: HNil

  // mapping from/to is trivial
  override def to(t: Class1): Repr = field[idw.T](t.getId) :: HNil
  override def from(r: Repr): Class1 = new Class1(r.head)
}

// create the singleton valuew for the field value
val valuew = Witness('value)

implicit val class2Generic = new LabelledGeneric[Class2] {
  // the generic representation of Class2 is the fields id of type String and value of type Int
  override type Repr = FieldType[idw.T, String] :: FieldType[valuew.T, Int] :: HNil

  // mapping from/to is trivial here too
  override def to(t: Class2): Repr =
    field[idw.T](t.getId) :: field[valuew.T](t.getValue) :: HNil
  override def from(r: Repr): Class2 = new Class2(r.head, r.tail.head)
}
```

Point 2. is trivial because both `Class1` and `Class2` don't have default values for fields:

```scala
implicit val class1Default = new Default.AsOptions[Class1] {
  override type Out = Option[String] :: HNil
  override def apply(): Out = None :: HNil
}

implicit val class2Default = new Default.AsOptions[Class2] {
  override type Out = Option[String] :: Option[Int] :: HNil
  override def apply(): Out = None :: None :: HNil
}
```

After we add this, PureConfig is able to load `Class1` and `Class2` from configuration.
The second and final part of this method is to add the generic representation of `Identifiable`
as a sealed family of `Class1` and `Class2`, or coproduct of them if you want:

```scala
val class1w = Witness('Class1)
val class2w = Witness('Class2)

implicit val identifiableGeneric = new LabelledGeneric[Identifiable] {
  override type Repr =
    FieldType[class1w.T, Class1] :+:
    FieldType[class2w.T, Class2] :+:
    CNil

  override def to(t: Identifiable): Repr = t match {
    case class1: Class1 => Inl(field[class1w.T](class1))
    case class2: Class2 => Inr(Inl(field[class2w.T](class2)))
  }

  override def from(r: Repr): Identifiable = r match {
    case Inl(class1) => class1
    case Inr(Inl(class2)) => class2
    case _ => ???
  }
}
```

**pro**: more efficient than method 2 because instead of using another data type,
it provides the missing pieces of information to PureConfig so that PureConfig
can derive the `ConfigReader[Identifiable]` by itself.

**cons**: shapeless magic. If you know shapeless, this is doable, otherwise I would be careful.

#### Summary

- **method 1** is good to add support for complex types with a similar structure of what is supported by PureConfig, e.g. products and coproducts;
- **method 2** is good to add support for simple types or types that don't need PureConfig's way to decode products (case classes) or coproducts (sealed families of classes);
- **method 3** is good when method 2 is too slow or when you need to add only part of the information to let PureConfig work, e.g. if you had the generic representation of `Class1` and you just have to add the default instance for the arguments.

The preferred method is **method 1** because it's the easiest to use and the more idiomatic.
In libraries like PureConfig, the best way to add a new data type is often to take something
supported by the library and "map it" to the new data type.
