package flightdatabase.syntax

import cats.data.{NonEmptyList => Nel}
import flightdatabase.syntax.string._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class StringOpsSpec extends AnyFlatSpec with Matchers {

  "asInt" should "parse valid (trimmed) integers and reject the rest" in {
    "42".asInt shouldBe Some(42)
    "  -7  ".asInt shouldBe Some(-7)
    "3.14".asInt shouldBe None
    "abc".asInt shouldBe None
    "".asInt shouldBe None
  }

  "asLong" should "parse valid (trimmed) longs and reject the rest" in {
    "9999999999".asLong shouldBe Some(9999999999L)
    "  10  ".asLong shouldBe Some(10L)
    "nope".asLong shouldBe None
  }

  "asDouble" should "parse valid doubles and reject the rest" in {
    "3.14".asDouble shouldBe Some(3.14)
    "-0.5".asDouble shouldBe Some(-0.5)
    "abc".asDouble shouldBe None
  }

  "asBigDecimal" should "parse valid (trimmed) big decimals and reject the rest" in {
    "1.5".asBigDecimal shouldBe Some(BigDecimal("1.5"))
    "  100  ".asBigDecimal shouldBe Some(BigDecimal(100))
    "x".asBigDecimal shouldBe None
  }

  "asBoolean" should "accept true/false/1/0 case-insensitively" in {
    "true".asBoolean shouldBe Some(true)
    "TRUE".asBoolean shouldBe Some(true)
    "1".asBoolean shouldBe Some(true)
    "false".asBoolean shouldBe Some(false)
    " False ".asBoolean shouldBe Some(false)
    "0".asBoolean shouldBe Some(false)
  }

  it should "reject anything else" in {
    "yes".asBoolean shouldBe None
    "2".asBoolean shouldBe None
    "".asBoolean shouldBe None
  }

  "toOption" should "return None for blank strings and Some otherwise" in {
    "".toOption shouldBe None
    "   ".toOption shouldBe None
    "value".toOption shouldBe Some("value")
  }

  "hasValue" should "reflect toOption" in {
    "value".hasValue shouldBe true
    "  ".hasValue shouldBe false
  }

  "trimEdges" should "drop the first and last characters" in {
    "(abc)".trimEdges shouldBe "abc"
    "[]".trimEdges shouldBe ""
  }

  "validRegex" should "detect compilable and broken regexes" in {
    "a.*b".validRegex shouldBe true
    "[".validRegex shouldBe false
  }

  "removeSubstring" should "remove every occurrence of the substring" in {
    "hello".removeSubstring("l") shouldBe "heo"
    "a.b.c".removeSubstring(".") shouldBe "abc"
    "abc".removeSubstring("z") shouldBe "abc"
  }

  "removeSeparator" should "remove word and non-word separators" in {
    "a,b,c".removeSeparator(',') shouldBe "abc"
    "a1b1c".removeSeparator('1') shouldBe "abc"
  }

  "removeQuotes" should "remove double quotes" in {
    "\"quoted\"".removeQuotes() shouldBe "quoted"
  }

  "removeSpaces" should "remove all spaces" in {
    "a b c".removeSpaces() shouldBe "abc"
  }

  "fixDecimalNotation" should "replace the separator with a dot" in {
    "1,5".fixDecimalNotation() shouldBe "1.5"
    " 2,75 ".fixDecimalNotation() shouldBe "2.75"
  }

  "deToEnNotation" should "convert German number notation to English" in {
    "1.234,56".deToEnNotation() shouldBe "1234.56"
  }

  "encodeSpacesInUrl" should "percent-encode spaces" in {
    "a b c".encodeSpacesInUrl() shouldBe "a%20b%20c"
  }

  "encodePlusInUrl" should "percent-encode plus signs" in {
    "a+b".encodePlusInUrl() shouldBe "a%2Bb"
  }

  "elseIfBlank" should "fall back to the default only when blank" in {
    "".elseIfBlank("default") shouldBe "default"
    "  ".elseIfBlank("default") shouldBe "default"
    "x".elseIfBlank("default") shouldBe "x"
  }

  "wrapInBrackets" should "surround the string with parentheses" in {
    "x".wrapInBrackets shouldBe "(x)"
  }

  "elseIfContains" should "fall back to the default only when the substring is present" in {
    "abc".elseIfContains("b", "default") shouldBe "default"
    "abc".elseIfContains("z", "default") shouldBe "abc"
  }

  "splitInTwo" should "return the first and last parts around the separator" in {
    "a-b".splitInTwo('-') shouldBe (("a", "b"))
    "a-b-c".splitInTwo('-') shouldBe (("a", "c"))
  }

  "splitToNel" should "split into a NonEmptyList" in {
    "a,b,c".splitToNel() shouldBe Nel.of("a", "b", "c")
    "single".splitToNel() shouldBe Nel.one("single")
    "a|b".splitToNel('|') shouldBe Nel.of("a", "b")
  }
}
