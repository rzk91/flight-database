package flightdatabase.syntax

import flightdatabase.syntax.iterable._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class IterableOpsSpec extends AnyFlatSpec with Matchers {

  "sumBy" should "sum the mapped values" in {
    List(1, 2, 3).sumBy(identity) shouldBe 6
    List("a", "bb", "ccc").sumBy(_.length) shouldBe 6
    List.empty[Int].sumBy(identity) shouldBe 0
  }

  it should "sum a mapped projection of domain objects" in {
    case class LineItem(quantity: Int, unitPrice: Double)
    val cart = List(LineItem(2, 1.5), LineItem(3, 2.0))
    cart.sumBy(_.quantity) shouldBe 5
    cart.sumBy(item => item.quantity * item.unitPrice) shouldBe 9.0
  }

  "averageBy" should "compute the mean of the mapped values" in {
    List(1, 2, 3).averageBy(identity) shouldBe Some(2.0)
    List(2, 4).averageBy(identity) shouldBe Some(3.0)
  }

  it should "return None for an empty collection" in {
    List.empty[Int].averageBy(identity) shouldBe None
  }

  "containsElement" should "detect membership" in {
    List(1, 2, 3).containsElement(2) shouldBe true
    List(1, 2, 3).containsElement(9) shouldBe false
    List.empty[Int].containsElement(1) shouldBe false
  }

  "countChanges" should "count adjacent differences" in {
    List(1, 1, 2, 2, 3).countChanges shouldBe 2
    List(1, 1, 1).countChanges shouldBe 0
    List(1).countChanges shouldBe 0
    List.empty[Int].countChanges shouldBe 0
  }

  it should "count changes of a mapped projection" in {
    List("a", "bb", "cc", "d").countChanges(_.length) shouldBe 2
  }

  it should "work on arrays via the array conversion" in {
    Array(1, 2, 2, 3).countChanges shouldBe 2
  }

  "percentageOf" should "compute the fraction matching a predicate" in {
    List(1, 2, 3, 4).percentageOf(_ % 2 == 0) shouldBe Some(0.5)
    List(1, 2, 3, 4).percentageOf(_ > 10) shouldBe Some(0.0)
  }

  it should "return None for an empty collection" in {
    List.empty[Int].percentageOf(_ > 0) shouldBe None
  }

  "length comparisons against an int" should "behave like lengthCompare" in {
    val xs = List(1, 2, 3)
    xs.lengthEquals(3) shouldBe true
    xs.lengthEquals(2) shouldBe false
    xs.shorterThan(4) shouldBe true
    xs.shorterThan(3) shouldBe false
    xs.longerThan(2) shouldBe true
    xs.longerThan(3) shouldBe false
    xs.equalToOrShorterThan(3) shouldBe true
    xs.equalToOrShorterThan(2) shouldBe false
    xs.equalToOrLongerThan(3) shouldBe true
    xs.equalToOrLongerThan(4) shouldBe false
  }

  "length comparisons against another collection" should "compare sizes" in {
    val xs = List(1, 2, 3)
    xs.lengthEquals(List("a", "b", "c")) shouldBe true
    xs.lengthEquals(List("a", "b")) shouldBe false
    xs.shorterThan(List(1, 2, 3, 4)) shouldBe true
    xs.longerThan(List(1, 2)) shouldBe true
    xs.equalToOrShorterThan(List(1, 2, 3)) shouldBe true
    xs.equalToOrLongerThan(List(1, 2, 3)) shouldBe true
  }

  it should "compare lazily, terminating against an infinite collection" in {
    // An eager implementation would hang on these; the lazy compare only forces as many
    // elements as it needs to decide the result.
    List(1, 2, 3).shorterThan(LazyList.from(1)) shouldBe true
    LazyList.from(1).longerThan(List(1, 2, 3)) shouldBe true
    LazyList.from(1).longerThan(4) shouldBe true
  }

  "minOf / maxOf" should "return the min/max of the mapped values" in {
    val xs = List("a", "bbb", "cc")
    xs.minOf(_.length) shouldBe 1
    xs.maxOf(_.length) shouldBe 3
  }

  "minOfOption / maxOfOption" should "be safe on empty collections" in {
    List("a", "bbb", "cc").minOfOption(_.length) shouldBe Some(1)
    List("a", "bbb", "cc").maxOfOption(_.length) shouldBe Some(3)
    List.empty[String].minOfOption(_.length) shouldBe None
    List.empty[String].maxOfOption(_.length) shouldBe None
  }
}
