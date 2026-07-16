package flightdatabase.syntax

import flightdatabase.syntax.try_._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

final class TryOpsSpec extends AnyFlatSpec with Matchers {

  private val ok: Try[Int] = Success(5)
  private val bad: Try[Int] = Failure(new RuntimeException("boom"))

  "exists" should "hold only for a matching success" in {
    ok.exists(_ > 3) shouldBe true
    ok.exists(_ > 10) shouldBe false
    bad.exists(_ => true) shouldBe false
  }

  "forall" should "hold for failures and matching successes" in {
    ok.forall(_ > 3) shouldBe true
    ok.forall(_ > 10) shouldBe false
    bad.forall(_ => false) shouldBe true
  }

  "contains" should "be true only when the success holds the value" in {
    ok.contains(5) shouldBe true
    ok.contains(6) shouldBe false
    bad.contains(5) shouldBe false
  }

  "toList" should "wrap a success and empty a failure" in {
    ok.toList shouldBe List(5)
    bad.toList shouldBe Nil
  }

  "toSet" should "wrap a success and empty a failure" in {
    ok.toSet shouldBe Set(5)
    bad.toSet shouldBe Set.empty[Int]
  }
}
