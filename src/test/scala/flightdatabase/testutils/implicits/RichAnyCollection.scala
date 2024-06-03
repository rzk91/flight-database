package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.{toApiResult, ApiResult}

final class RichAnyCollection[C[_], A](private val ca: C[A]) extends AnyVal {
  def asResult[F[_]](implicit F: Applicative[F]): F[ApiResult[C[A]]] = F.pure(toApiResult(ca))
}
