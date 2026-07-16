package flightdatabase

import flightdatabase.Operator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class FieldTypeSpec extends AnyFlatSpec with Matchers {

  "asString" should "give the human-readable type name" in {
    StringType.asString shouldBe "String"
    IntType.asString shouldBe "Int"
    LongType.asString shouldBe "Long"
    BooleanType.asString shouldBe "Boolean"
    BigDecimalType.asString shouldBe "BigDecimal"
  }

  "StringType operators" should "cover text-oriented operators only" in {
    StringType.operators shouldBe Set(
      Equals,
      NotEquals,
      RegexMatch,
      In,
      NotIn,
      StartsWith,
      EndsWith,
      Contains,
      NotContains
    )
    StringType.operators should not contain GreaterThan
  }

  "BooleanType operators" should "be limited to equality and IS checks" in {
    BooleanType.operators shouldBe Set(Equals, NotEquals, Is, IsNot)
  }

  "numeric type operators" should "include ordering and range operators" in {
    List(IntType, LongType, BigDecimalType).foreach { ft =>
      (ft.operators should contain).allOf(GreaterThan, LessThan, Range, In, NotIn)
      ft.operators should not contain RegexMatch
      ft.operators should not contain Is
    }
  }

  "the shared FieldType-to-operator map" should "cover every field type with a non-empty set" in {
    FieldType.fieldTypeToOperators.keySet shouldBe Set(
      StringType,
      IntType,
      LongType,
      BooleanType,
      BigDecimalType
    )
    FieldType.fieldTypeToOperators.values.foreach(_ should not be empty)
  }

  it should "give Int and Long identical operator sets" in {
    IntType.operators shouldBe LongType.operators
  }
}
