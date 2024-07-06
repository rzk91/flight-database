package flightdatabase.domain

case class ValidatedSortAndLimit(
  sortBy: Option[String],
  order: Option[ResultOrder],
  limit: Option[Long],
  offset: Option[Long]
)

object ValidatedSortAndLimit {
  // Test helpers
  def empty: ValidatedSortAndLimit = ValidatedSortAndLimit(None, None, None, None)

  def sort(by: String): ValidatedSortAndLimit =
    ValidatedSortAndLimit(Some(by), None, None, None)

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
