package flightdatabase.partial

import cats.data.{NonEmptyList => Nel}
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit

trait PartiallyAppliedGetBy[F[_], T] {

  def apply[V](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit,
    fieldType: FieldType[V]
  ): F[ApiResult[Nel[T]]]
}
