package flightdatabase.api

import org.http4s.dsl.impl.FlagQueryParamMatcher

package object endpoints {
  object OnlyNamesFlagMatcher extends FlagQueryParamMatcher("only-names")
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")
}
