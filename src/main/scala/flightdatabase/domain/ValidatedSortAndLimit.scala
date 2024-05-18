package flightdatabase.domain

import doobie.Fragment
import doobie.implicits._

case class ValidatedSortAndLimit(
  sortBy: Option[String],
  order: Option[ResultOrder],
  limit: Option[Long],
  offset: Option[Long]
) {

  def fragment: Fragment = {
    val sort = sortBy.fold(fr"") { s =>
      val ord = order.getOrElse(ResultOrder.Ascending).entryName
      fr"ORDER BY" ++ Fragment.const(s) ++ Fragment.const(ord)
    }
    val lim = limit.filter(_ > 0).fold(fr"")(l => fr"LIMIT $l")
    val off = offset.filter(_ > 0).fold(fr"")(o => fr"OFFSET $o")
    sort ++ lim ++ off
  }
}

object ValidatedSortAndLimit {
  // Test helpers
  def empty: ValidatedSortAndLimit = ValidatedSortAndLimit(None, None, None, None)

  def sortAscending(sortBy: String): ValidatedSortAndLimit =
    ValidatedSortAndLimit(Some(sortBy), Some(ResultOrder.Ascending), None, None)

  def sortDescending(sortBy: String): ValidatedSortAndLimit =
    ValidatedSortAndLimit(Some(sortBy), Some(ResultOrder.Descending), None, None)

  def limit(limit: Long): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, Some(limit), None)

  def offset(offset: Long): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, None, Some(offset))

  def limitAndOffset(limit: Long, offset: Long): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, Some(limit), Some(offset))
}
