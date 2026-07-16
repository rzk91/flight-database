package flightdatabase.syntax

import flightdatabase.syntax.option._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class OptionOpsSpec extends AnyFlatSpec with Matchers {

  "debug" should "render the contained value" in {
    Some(5).debug shouldBe "5"
    Some("hello").debug shouldBe "hello"
  }

  it should "render N/A for None" in {
    (None: Option[Int]).debug shouldBe "N/A"
  }
}
