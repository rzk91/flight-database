package flightdatabase.testutils.implicits

import cats.Applicative
import flightdatabase.domain.ApiResult
import flightdatabase.domain.Got

final class RichAny[A](private val a: A) extends AnyVal {
  def asResult[F[_]: Applicative]: F[ApiResult[A]] = Got(a).elevate[F]
}
