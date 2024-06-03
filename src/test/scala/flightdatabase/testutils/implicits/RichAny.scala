package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.ApiResult
import flightdatabase.domain.toApiResult

final class RichAny[A](private val a: A) extends AnyVal {
  def asResult[F[_]](implicit F: Applicative[F]): F[ApiResult[A]] = F.pure(toApiResult(a))
}
