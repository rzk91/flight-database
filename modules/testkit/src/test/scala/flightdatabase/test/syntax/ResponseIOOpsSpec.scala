package flightdatabase.test.syntax

import cats.effect.IO
import flightdatabase.test.syntax.response._
import org.http4s.Response
import org.http4s.Status
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class ResponseIOOpsSpec extends AnyFlatSpec with Matchers {

  "string" should "extract the response body as text" in {
    Response[IO](Status.Ok).withEntity("hello").string shouldBe "hello"
  }

  it should "be empty for a body-less response" in {
    Response[IO](Status.NoContent).string shouldBe ""
  }

  "extract" should "decode the body via an implicit EntityDecoder" in {
    Response[IO](Status.Ok).withEntity("payload").extract[String] shouldBe "payload"
  }

  it should "leave the response's status untouched" in {
    val created = Response[IO](Status.Created).withEntity("42")
    created.status shouldBe Status.Created
    created.extract[String] shouldBe "42"
  }
}
