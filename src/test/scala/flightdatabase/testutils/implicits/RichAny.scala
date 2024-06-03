package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.{toApiResult, ApiResult}

final class RichAny[A](private val a: A) extends AnyVal {
  def asResult[F[_]](implicit F: Applicative[F]): F[ApiResult[A]] = F.pure(toApiResult(a))
}
