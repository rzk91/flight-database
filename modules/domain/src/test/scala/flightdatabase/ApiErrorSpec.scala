package flightdatabase

import cats.Id
import cats.data.{NonEmptyList => Nel}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ApiErrorSpec extends AnyFlatSpec with Matchers {

  "constant ApiErrors" should "expose their fixed messages" in {
    EntryAlreadyExists.error should include("already exists")
    EntryCheckFailed.error should include("cannot be blank")
    EntryNullCheckFailed.error should include("cannot be null")
    EntryInvalidFormat.error should include("invalid format")
    EntryListEmpty.error shouldBe "No entries found"
    EntryHasInvalidForeignKey.error should include("foreign key")
    FeatureNotImplemented.error should include("development")
  }

  "InconsistentIds" should "mention both ids" in {
    InconsistentIds(1L, 2L).error should (include("1").and(include("2")))
  }

  "EntryNotFound" should "render the missing entry" in {
    EntryNotFound("XYZ").error should include("XYZ")
  }

  "EntryValueTooLong" should "name the offending field when present" in {
    EntryValueTooLong(Some("name")).error should include("name")
    EntryValueTooLong(None).error should include("A value")
  }

  "InvalidTimezone / InvalidField / InvalidValueType" should "echo their argument" in {
    InvalidTimezone("Mars/Phobos").error should include("Mars/Phobos")
    InvalidField("weird").error should include("weird")
    InvalidValueType("??").error should include("??")
  }

  "WrongOperator" should "mention the operator, field and type" in {
    val err = WrongOperator(Operator.Equals, "age", IntType)
    err.error should (include("age").and(include("IntType")))
  }

  "UnknownDbError" should "carry its raw error verbatim" in {
    UnknownDbError("something broke").error shouldBe "something broke"
  }

  "SqlError" should "include the state code and a docs link" in {
    val err = SqlError("42601", None, None)
    err.error should include("42601")
    err.error should include("postgresql.org")
    err.addendum shouldBe ""
  }

  it should "build an addendum from the relevant field and values" in {
    SqlError("23505", Some("iso"), Some(Nel.of("DE", "IN"))).addendum should (
      include("iso").and(include("DE, IN"))
    )
    SqlError("23505", Some("iso"), None).addendum should include("iso")
    SqlError("23505", None, Some(Nel.of("DE"))).addendum should include("DE")
  }

  "asResult" should "lift an error into the left of an ApiResult" in {
    EntryListEmpty.asResult[Int] shouldBe Left(EntryListEmpty)
  }

  "elevate" should "wrap the result in the applicative" in {
    EntryListEmpty.elevate[Id, Int] shouldBe Left(EntryListEmpty)
  }
}
