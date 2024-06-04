package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.ApiResult
import flightdatabase.domain.Got

final class RichAnyCollection[C[_], A](private val ca: C[A]) extends AnyVal {
  def asResult[F[_]: Applicative]: F[ApiResult[C[A]]] = Got(ca).elevate[F]
}
