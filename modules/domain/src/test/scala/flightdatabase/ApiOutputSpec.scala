package flightdatabase

import cats.Id
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ApiOutputSpec extends AnyFlatSpec with Matchers {

  "value" should "expose the wrapped payload" in {
    Created(42).value shouldBe 42
    Got("hello").value shouldBe "hello"
    Updated(List(1, 2, 3)).value shouldBe List(1, 2, 3)
    Deleted.value shouldBe (())
  }

  "asResult" should "lift an output into the right of an ApiResult" in {
    Got(42).asResult shouldBe Right(Got(42))
    Created("x").asResult shouldBe Right(Created("x"))
    Updated(true).asResult shouldBe Right(Updated(true))
    Deleted.asResult shouldBe Right(Deleted)
  }

  "elevate" should "wrap the result in the applicative" in {
    Got(42).elevate[Id] shouldBe Right(Got(42))
    Deleted.elevate[Id] shouldBe Right(Deleted)
  }

  "the success and error sides" should "produce complementary Either results" in {
    Got(1).asResult.isRight shouldBe true
    EntryListEmpty.asResult[Int].isLeft shouldBe true
  }
}
