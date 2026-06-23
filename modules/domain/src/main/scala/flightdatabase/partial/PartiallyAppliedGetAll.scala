package flightdatabase.partial

import cats.data.{NonEmptyList => Nel}
import doobie.Read
import flightdatabase.ApiResult
import flightdatabase.ValidatedSortAndLimit

trait PartiallyAppliedGetAll[F[_], T] {

  def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[T]]]

  def apply[V: Read](sortAndLimit: ValidatedSortAndLimit, returnField: String): F[ApiResult[Nel[V]]]
}
