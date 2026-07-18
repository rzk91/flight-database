package flightdatabase.test.syntax

import cats.effect.IO
import flightdatabase.ApiResult
import flightdatabase.EntryListEmpty
import flightdatabase.Got
import flightdatabase.test.syntax.ioresult._
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IOResultOpsSpec extends AnyFlatSpec with Matchers {

  private val success: IO[ApiResult[Int]] = IO.pure(Got(42).asResult)
  private val failure: IO[ApiResult[Int]] = IO.pure(EntryListEmpty.asResult[Int])

  "value" should "unwrap the payload of a successful result" in {
    success.value shouldBe 42
  }

  it should "fail when the result is a Left" in {
    a[TestFailedException] should be thrownBy failure.value
  }

  "error" should "extract the ApiError of a failed result" in {
    failure.error shouldBe EntryListEmpty
  }

  it should "fail when the result is a Right" in {
    a[TestFailedException] should be thrownBy success.error
  }
}
