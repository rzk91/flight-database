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
      val ord = order.getOrElse(ResultOrder.Ascending).toString.toUpperCase()
      fr"ORDER BY $s $ord"
    }
    val lim = limit.fold(fr"")(l => fr"LIMIT $l")
    val off = offset.fold(fr"")(o => fr"OFFSET $o")
    sort ++ lim ++ off
  }
}
