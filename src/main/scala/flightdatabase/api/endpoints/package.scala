package flightdatabase.api

import org.http4s.dsl.impl.FlagQueryParamMatcher
import org.http4s.dsl.impl.QueryParamDecoderMatcherWithDefault

package object endpoints {
  object OnlyNamesFlagMatcher extends FlagQueryParamMatcher("only-names")
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")

  object FieldMatcherNameDefault
      extends QueryParamDecoderMatcherWithDefault[String]("field", "name")

  object FieldMatcherIdDefault
      extends QueryParamDecoderMatcherWithDefault[String]("field", "id")
}
