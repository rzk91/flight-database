package flightdatabase.test.syntax

import cats.data.{NonEmptyList => Nel}
import flightdatabase.test.syntax.matchers._
import org.scalatest.Inspectors._
import org.scalatest.LoneElement._
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class MatchersSpec extends AnyFlatSpec with Matchers {

  private val single: Nel[Int] = Nel.one(1)
  private val many: Nel[Int] = Nel.of(1, 2, 3)

  "Containing[Nel]" should "detect membership via contain" in {
    many should contain(2)
    many should not contain 9
    single should contain(1)
  }

  it should "support contain oneOf / noneOf" in {
    (many should contain).oneOf(9, 2, 8)
    many should contain noneOf (7, 8, 9)
  }

  "Aggregating[Nel]" should "support contain only, regardless of order" in {
    many should contain only (3, 1, 2)
    many should not(contain only (1, 2))
  }

  it should "support contain allOf / atLeastOneOf / atMostOneOf" in {
    (many should contain).allOf(1, 3)
    (many should contain).atLeastOneOf(1, 99)
    (many should contain).atMostOneOf(1, 99)
  }

  it should "support contain theSameElementsAs another collection" in {
    many should contain theSameElementsAs List(3, 2, 1)
    many should not(contain theSameElementsAs List(1, 2))
  }

  "Collecting[Nel]" should "expose loneElement for a single-element Nel" in {
    single.loneElement shouldBe 1
  }

  it should "fail loneElement for a multi-element Nel" in {
    a[TestFailedException] should be thrownBy many.loneElement
  }

  it should "support Inspectors traversal over a Nel" in {
    forAll(many)(_ should be > 0)
    forExactly(1, many)(_ shouldBe 2)
    forAll(single)(_ shouldBe 1)
  }

  it should "fail an Inspectors count check that doesn't hold" in {
    a[TestFailedException] should be thrownBy forExactly(2, many)(_ shouldBe 2)
  }

  // No ScalaTest matcher routes through Collecting#sizeOf (`have size` resolves against Nel's
  // own size method instead), so it is exercised directly here rather than via the DSL.
  it should "report the correct size via Collecting#sizeOf" in {
    collectingNel[Int].sizeOf(single) shouldBe 1
    collectingNel[Int].sizeOf(many) shouldBe 3
  }
}
