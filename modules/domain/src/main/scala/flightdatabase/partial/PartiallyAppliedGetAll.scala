package flightdatabase.partial

import cats.data.{NonEmptyList => Nel}
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.ValidatedSortAndLimit

trait PartiallyAppliedGetAll[F[_], T] {

  def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[T]]]

  def apply[V](
    sortAndLimit: ValidatedSortAndLimit,
    returnField: String,
    fieldType: FieldType[V]
  ): F[ApiResult[Nel[V]]]
}
