package flightdatabase.syntax

import flightdatabase.syntax.double._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class DoubleOpsSpec extends AnyFlatSpec with Matchers {

  "isDefined" should "be true for finite doubles" in {
    1.0.isDefined shouldBe true
    0.0.isDefined shouldBe true
    (-42.5).isDefined shouldBe true
    Double.MaxValue.isDefined shouldBe true
    Double.MinValue.isDefined shouldBe true
  }

  it should "be false for NaN and infinities" in {
    Double.NaN.isDefined shouldBe false
    (0.0 / 0.0).isDefined shouldBe false
    Double.PositiveInfinity.isDefined shouldBe false
    Double.NegativeInfinity.isDefined shouldBe false
    (1.0 / 0.0).isDefined shouldBe false
  }

  "isUndefined" should "be the negation of isDefined" in {
    1.0.isUndefined shouldBe false
    0.0.isUndefined shouldBe false
    Double.NaN.isUndefined shouldBe true
    Double.PositiveInfinity.isUndefined shouldBe true
    Double.NegativeInfinity.isUndefined shouldBe true
  }
}
