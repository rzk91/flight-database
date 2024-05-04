package flightdatabase.domain

sealed trait FieldType
case object StringType extends FieldType
case object IntType extends FieldType
case object LongType extends FieldType
case object BooleanType extends FieldType
case object BigDecimalType extends FieldType
