package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.ApiResult
import flightdatabase.domain.toApiResult

final class RichAnyCollection[C[_], A](private val ca: C[A]) extends AnyVal {
  def asResult[F[_]](implicit F: Applicative[F]): F[ApiResult[C[A]]] = F.pure(toApiResult(ca))
}
