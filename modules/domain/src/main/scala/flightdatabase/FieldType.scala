package flightdatabase

import flightdatabase.Operator._

sealed abstract class FieldType[A](val asString: String) {
  lazy val operators: Set[Operator] = FieldType.fieldTypeToOperators(this)
}
case object StringType extends FieldType[String]("String")
case object IntType extends FieldType[Int]("Int")
case object LongType extends FieldType[Long]("Long")
case object BooleanType extends FieldType[Boolean]("Boolean")
case object BigDecimalType extends FieldType[BigDecimal]("BigDecimal")

object FieldType {

  val fieldTypeToOperators: Map[FieldType[_], Set[Operator]] = Map(
    StringType -> Set(
      Equals,
      NotEquals,
      RegexMatch,
      In,
      NotIn,
      StartsWith,
      EndsWith,
      Contains,
      NotContains
    ),
    IntType -> Set(
      Equals,
      NotEquals,
      GreaterThan,
      GreaterThanOrEqualTo,
      LessThan,
      LessThanOrEqualTo,
      Range,
      In,
      NotIn
    ),
    LongType -> Set(
      Equals,
      NotEquals,
      GreaterThan,
      GreaterThanOrEqualTo,
      LessThan,
      LessThanOrEqualTo,
      Range,
      In,
      NotIn
    ),
    BooleanType -> Set(Equals, NotEquals, Is, IsNot),
    BigDecimalType -> Set(
      Equals,
      NotEquals,
      GreaterThan,
      GreaterThanOrEqualTo,
      LessThan,
      LessThanOrEqualTo,
      Range,
      In,
      NotIn
    )
  )
}
