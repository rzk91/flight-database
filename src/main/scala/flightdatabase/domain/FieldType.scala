package flightdatabase.domain

import flightdatabase.api.Operator
import flightdatabase.api.Operator._

sealed abstract class FieldType(val asString: String) {
  lazy val operators: Set[Operator] = FieldType.fieldTypeToOperators(this)
}
case object StringType extends FieldType("String")
case object IntType extends FieldType("Int")
case object LongType extends FieldType("Long")
case object BooleanType extends FieldType("Boolean")
case object BigDecimalType extends FieldType("BigDecimal")

object FieldType {

  val fieldTypeToOperators: Map[FieldType, Set[Operator]] = Map(
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
