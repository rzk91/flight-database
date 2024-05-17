package flightdatabase.domain

import doobie.Fragment
import doobie.implicits._

case class ValidatedSortAndLimit(
  sortBy: Option[String],
  order: Option[ResultOrder],
  limit: Option[Int],
  offset: Option[Int]
) {

  def fragment: Fragment = {
    val sort = sortBy.fold(fr"") { s =>
      val ord = order.getOrElse(ResultOrder.Ascending).entryName.toUpperCase()
      fr"ORDER BY" ++ Fragment.const(s) ++ Fragment.const(ord)
    }
    val lim = limit.fold(fr"")(l => fr"LIMIT $l")
    val off = offset.fold(fr"")(o => fr"OFFSET $o")
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

  def limit(limit: Int): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, Some(limit), None)

  def offset(offset: Int): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, None, Some(offset))

  def limitAndOffset(limit: Int, offset: Int): ValidatedSortAndLimit =
    ValidatedSortAndLimit(None, None, Some(limit), Some(offset))
}
