package flightdatabase.testutils

import cats.effect.IO
import flightdatabase.domain.ApiResult

package object implicits {

  @inline implicit def enrichIOOperation[A](op: IO[ApiResult[A]]): RichIOOperation[A] =
    new RichIOOperation(op)
}
