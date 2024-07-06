package flightdatabase.partial

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.ApiResult
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit

trait PartiallyAppliedGetBy[F[_], T] {

  def apply[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): F[ApiResult[Nel[T]]]
}
