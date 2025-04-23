# Spark module for PureConfig

Adds support for selected [Spark](http://spark.apache.org/) classes to PureConfig.

## Add pureconfig-spark to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-spark" % "0.17.9"
```

Also, `pureconfig-spark` depends on `spark-sql` with `provided` scope.
Spark libraries are generally added on runtime.
This module has been tested on Spark 3 but it should also work for Spark 2.4 since basic datatype APIs should stay the same.

To use the Spark module you need to import:
```scala
import pureconfig.module.spark._
```

## Supported classes

* `org.apache.spark.sql.types.DataType`
* `org.apache.spark.sql.types.StructType`
* `org.apache.spark.sql.types.Metadata`
* `org.apache.spark.sql.types.StructField`

## Example

### Custom HOCON schema to/from Spark schema
Setup custom schema case classes and converters between custom schema and Spark schema.
```scala
import org.apache.spark.sql.types._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.spark.sql._
import com.typesafe.config.ConfigRenderOptions

case class MySchema(name: String, fields: List[StructField], someOtherSetting: Option[String])

def mySchemaToSparkSchema(schema: MySchema): StructType =
  StructType(schema.fields)

def sparkSchemaToMySchema(name: String, schema: StructType): MySchema =
  MySchema(name, schema.fields.toList, None)

val renderOpt = ConfigRenderOptions.defaults.setOriginComments(false)
```

Convert custom schema to Spark and back to custom schema. Resultant string schema should match original source.
```scala
val mySchemaRes = ConfigSource.string(
  """name: Employee,
    |fields: [
    |  { name: name, data-type: string }, #types are case-insensitive and some types have variations/truncations
    |  { name: age, data-type: integer, nullable = false, metadata = "{\"k\": \"v\"}" }, #also note that `nullable` and `metadata` are optional fields with Spark defaults
    |  { name: salary, data-type: "decimal(6,2)" },
    |  { name: address, data-type: "line1 string, line2 string" } #outer `struct` is optional
    |]
    |""".stripMargin).load[MySchema]
// mySchemaRes: ConfigReader.Result[MySchema] = Right(
//   value = MySchema(
//     name = "Employee",
//     fields = List(
//       StructField(
//         name = "name",
//         dataType = StringType,
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "age",
//         dataType = IntegerType,
//         nullable = false,
//         metadata = {"k":"v"}
//       ),
//       StructField(
//         name = "salary",
//         dataType = DecimalType(precision = 6, scale = 2),
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "address",
//         dataType = Seq(
//           StructField(
//             name = "line1",
//             dataType = StringType,
//             nullable = true,
//             metadata = {}
//           ),
//           StructField(
//             name = "line2",
//             dataType = StringType,
//             nullable = true,
//             metadata = {}
//           )
//         ),
//         nullable = true,
//         metadata = {}
//       )
//     ),
//     someOtherSetting = None
//   )
// )

val sparkSchemaRes = mySchemaRes.map(mySchemaToSparkSchema)
// sparkSchemaRes: Either[error.ConfigReaderFailures, StructType] = Right(
//   value = Seq(
//     StructField(
//       name = "name",
//       dataType = StringType,
//       nullable = true,
//       metadata = {}
//     ),
//     StructField(
//       name = "age",
//       dataType = IntegerType,
//       nullable = false,
//       metadata = {"k":"v"}
//     ),
//     StructField(
//       name = "salary",
//       dataType = DecimalType(precision = 6, scale = 2),
//       nullable = true,
//       metadata = {}
//     ),
//     StructField(
//       name = "address",
//       dataType = Seq(
//         StructField(
//           name = "line1",
//           dataType = StringType,
//           nullable = true,
//           metadata = {}
//         ),
//         StructField(
//           name = "line2",
//           dataType = StringType,
//           nullable = true,
//           metadata = {}
//         )
//       ),
//       nullable = true,
//       metadata = {}
//     )
//   )
// )

val mySchemaRes2 =
  for {
    mySchema <- mySchemaRes
    sparkSchema <- sparkSchemaRes
  } yield sparkSchemaToMySchema(mySchema.name, sparkSchema)
// mySchemaRes2: Either[error.ConfigReaderFailures, MySchema] = Right(
//   value = MySchema(
//     name = "Employee",
//     fields = List(
//       StructField(
//         name = "name",
//         dataType = StringType,
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "age",
//         dataType = IntegerType,
//         nullable = false,
//         metadata = {"k":"v"}
//       ),
//       StructField(
//         name = "salary",
//         dataType = DecimalType(precision = 6, scale = 2),
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "address",
//         dataType = Seq(
//           StructField(
//             name = "line1",
//             dataType = StringType,
//             nullable = true,
//             metadata = {}
//           ),
//           StructField(
//             name = "line2",
//             dataType = StringType,
//             nullable = true,
//             metadata = {}
//           )
//         ),
//         nullable = true,
//         metadata = {}
//       )
//     ),
//     someOtherSetting = None
//   )
// )

val stringSchemaRes = mySchemaRes2.map(ConfigWriter[MySchema].to(_).render(renderOpt))
// stringSchemaRes: Either[error.ConfigReaderFailures, String] = Right(
//   value = """{
//     "fields" : [
//         {
//             "data-type" : "STRING",
//             "metadata" : "{}",
//             "name" : "name",
//             "nullable" : true
//         },
//         {
//             "data-type" : "INT",
//             "metadata" : "{\"k\":\"v\"}",
//             "name" : "age",
//             "nullable" : false
//         },
//         {
//             "data-type" : "DECIMAL(6,2)",
//             "metadata" : "{}",
//             "name" : "salary",
//             "nullable" : true
//         },
//         {
//             "data-type" : "STRUCT<line1: STRING, line2: STRING>",
//             "metadata" : "{}",
//             "name" : "address",
//             "nullable" : true
//         }
//     ],
//     "name" : "Employee"
// }
// """
// )
```

Note: `containsNull`/`nullable`/`metadata` optional fields for `ArrayType` or `SructFields` within `StructType` will be lost from encoding as Spark does not encode them in their DDL encoding.
```scala
case class MyConfig(field: StructField, obj: DataType, arr: DataType)
val meta = Metadata.fromJson("{\"k\": \"v\"}")
// meta: Metadata = {"k":"v"}

val myConfigString = ConfigWriter[MyConfig].to(MyConfig(
  StructField("a", StringType, nullable = false, metadata = meta), //nullable/metadata will be kept within HOCON structure
  StructType(StructField("b", StringType, nullable = false, metadata = meta) :: Nil), //nullable/metadata will be lost from DDL string encoding
  ArrayType(StringType, containsNull = false) //containsNull will be lost
)).render(renderOpt)
// myConfigString: String = """{
//     "arr" : "ARRAY<STRING>",
//     "field" : {
//         "data-type" : "STRING",
//         "metadata" : "{\"k\":\"v\"}",
//         "name" : "a",
//         "nullable" : false
//     },
//     "obj" : "STRUCT<b: STRING>"
// }
// """

//lost optional values will be set to their defaults
val myConfigRes = ConfigSource.string(myConfigString).load[MyConfig]
// myConfigRes: ConfigReader.Result[MyConfig] = Right(
//   value = MyConfig(
//     field = StructField(
//       name = "a",
//       dataType = StringType,
//       nullable = false,
//       metadata = {"k":"v"}
//     ),
//     obj = Seq(
//       StructField(
//         name = "b",
//         dataType = StringType,
//         nullable = true,
//         metadata = {}
//       )
//     ),
//     arr = ArrayType(elementType = StringType, containsNull = true)
//   )
// )
```

### Full schema encoded as HOCON String field to/from Spark schema
You can also read Spark schemas directly as `StructType` instead of narrowing `DataType` yourself.
Do note that the outer unlike `DataType`, the list of fields cannot be wrapped by an outer `STRUCT<...>`.
```scala
case class Config(schema: StructType)

val configRes = ConfigSource.string(
  """
    |# "struct<a:int,b:string,c:struct<c1:int,c2:double>>" will not work here
    |schema = "a int, b string, c struct<c1:int,c2:double>"
    |""".stripMargin).load[Config]
// configRes: ConfigReader.Result[Config] = Right(
//   value = Config(
//     schema = Seq(
//       StructField(
//         name = "a",
//         dataType = IntegerType,
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "b",
//         dataType = StringType,
//         nullable = true,
//         metadata = {}
//       ),
//       StructField(
//         name = "c",
//         dataType = Seq(
//           StructField(
//             name = "c1",
//             dataType = IntegerType,
//             nullable = true,
//             metadata = {}
//           ),
//           StructField(
//             name = "c2",
//             dataType = DoubleType,
//             nullable = true,
//             metadata = {}
//           )
//         ),
//         nullable = true,
//         metadata = {}
//       )
//     )
//   )
// )
    
val stringSchemaRes2 = configRes.map(ConfigWriter[Config].to(_).render(renderOpt))
// stringSchemaRes2: Either[error.ConfigReaderFailures, String] = Right(
//   value = """{
//     "schema" : "a INT,b STRING,c STRUCT<c1: INT, c2: DOUBLE>"
// }
// """
// )
```
