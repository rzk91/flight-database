package flightdatabase.domain

import flightdatabase.api.Operator
import flightdatabase.api.Operator._

sealed trait FieldType {
  lazy val operators: Set[Operator] = FieldType.fieldTypeToOperators(this)
}
case object StringType extends FieldType
case object IntType extends FieldType
case object LongType extends FieldType
case object BooleanType extends FieldType
case object BigDecimalType extends FieldType

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
