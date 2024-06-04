package flightdatabase.testutils

import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher

trait CustomMatchers {

  class IncludeAllOf(expectedSubstrings: String*) extends Matcher[String] {

    def apply(left: String): MatchResult =
      MatchResult(
        expectedSubstrings.forall(left.contains),
        s"""String "$left" did not include all of those substrings: ${expectedSubstrings
          .map(s => s""""$s"""")
          .mkString(", ")}""",
        s"""String "$left" contained all of those substrings: ${expectedSubstrings
          .map(s => s""""$s"""")
          .mkString(", ")}"""
      )
  }

  def includeAllOf(expectedSubstrings: String*): IncludeAllOf =
    new IncludeAllOf(expectedSubstrings: _*)
}

object CustomMatchers extends CustomMatchers
