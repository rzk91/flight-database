package flightdatabase.domain.partial

import cats.data.{NonEmptyList => Nel}
import doobie.Read
import flightdatabase.domain.ApiResult
import flightdatabase.domain.ValidatedSortAndLimit

trait PartiallyAppliedGetAll[F[_], T] {

  def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[T]]]

  def apply[V: Read](sortAndLimit: ValidatedSortAndLimit, returnField: String): F[ApiResult[Nel[V]]]
}
