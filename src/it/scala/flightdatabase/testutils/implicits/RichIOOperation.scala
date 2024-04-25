package flightdatabase.testutils.implicits

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiError
import flightdatabase.domain.ApiResult
import org.scalatest.EitherValues._

class RichIOOperation[A](private val op: IO[ApiResult[A]]) extends AnyVal {
  def value: A = op.unsafeRunSync().value.value
  def error: ApiError = op.unsafeRunSync().left.value
}
