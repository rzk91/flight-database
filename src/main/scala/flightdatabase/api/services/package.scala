package flightdatabase.api

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.dsl.{Http4sDsl, RequestDslBinCompat}

package object services {
  type Http4sDslT[F[_]] = Http4sDsl[F] with RequestDslBinCompat

  object OnlyNameQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("only-names")
}
