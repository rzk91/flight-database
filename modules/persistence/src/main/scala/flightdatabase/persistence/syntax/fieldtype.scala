package flightdatabase.persistence.syntax

import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import flightdatabase.BigDecimalType
import flightdatabase.BooleanType
import flightdatabase.FieldType
import flightdatabase.IntType
import flightdatabase.LongType
import flightdatabase.StringType

final class FieldTypeOps[A](private val fieldType: FieldType[A]) extends AnyVal {
  def asPut: Put[A] = FieldTypeOps.putFor(fieldType)
  def asRead: Read[A] = FieldTypeOps.readFor(fieldType)
}

object FieldTypeOps {

  def putFor[A](ft: FieldType[A]): Put[A] = ft match {
    case StringType     => Put[String]
    case IntType        => Put[Int]
    case LongType       => Put[Long]
    case BooleanType    => Put[Boolean]
    case BigDecimalType => Put[BigDecimal]
  }

  def readFor[A](ft: FieldType[A]): Read[A] = ft match {
    case StringType     => Read[String]
    case IntType        => Read[Int]
    case LongType       => Read[Long]
    case BooleanType    => Read[Boolean]
    case BigDecimalType => Read[BigDecimal]
  }
}

trait ToFieldTypeOps {

  @inline implicit def toFieldTypeOps[A](fieldType: FieldType[A]): FieldTypeOps[A] =
    new FieldTypeOps(fieldType)
}

object fieldtype extends ToFieldTypeOps
