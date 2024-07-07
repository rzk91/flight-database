package flightdatabase.domain.partial

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.ValidatedSortAndLimit

trait PartiallyAppliedGetBy[F[_], T] {

  def apply[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): F[ApiResult[Nel[T]]]
}
