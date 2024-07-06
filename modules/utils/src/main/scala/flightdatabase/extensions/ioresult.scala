package flightdatabase.extensions

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.ApiError
import flightdatabase.ApiResult
import org.scalatest.EitherValues._

final class IOResultOps[A](private val op: IO[ApiResult[A]]) extends AnyVal {
  def value: A = op.unsafeRunSync().value.value
  def error: ApiError = op.unsafeRunSync().left.value
}

trait ToIOResultOps {

  @inline implicit def toIOResultOps[A](op: IO[ApiResult[A]]): IOResultOps[A] =
    new IOResultOps(op)
}

object ioresult extends ToIOResultOps
