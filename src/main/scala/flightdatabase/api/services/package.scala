package flightdatabase.api

import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.RequestDslBinCompat
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

package object services {
  type Http4sDslT[F[_]] = Http4sDsl[F] with RequestDslBinCompat

  object OnlyNameQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("only-names")
}
