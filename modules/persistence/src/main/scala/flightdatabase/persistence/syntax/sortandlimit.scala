package flightdatabase.persistence.syntax

import doobie.Fragment
import doobie.syntax.string._
import flightdatabase.ResultOrder
import flightdatabase.ValidatedSortAndLimit

final class SortAndLimitOps(private val validatedSortAndLimit: ValidatedSortAndLimit)
    extends AnyVal {

  def fragment: Fragment = {
    val sort = validatedSortAndLimit.sortBy.fold(fr"") { s =>
      val ord = validatedSortAndLimit.order.getOrElse(ResultOrder.Ascending).entryName
      fr"ORDER BY" ++ Fragment.const(s) ++ Fragment.const(ord) ++
      fr"," ++ Fragment.const("id") ++ Fragment.const(ord) // Make sorting stable
    }
    val lim = validatedSortAndLimit.limit.filter(_ > 0).fold(fr"")(l => fr"LIMIT $l")
    val off = validatedSortAndLimit.offset.filter(_ > 0).fold(fr"")(o => fr"OFFSET $o")
    sort ++ lim ++ off
  }
}

trait ToSortAndLimitOps {

  @inline implicit def toSortAndLimitOps(
    validatedSortAndLimit: ValidatedSortAndLimit
  ): SortAndLimitOps =
    new SortAndLimitOps(validatedSortAndLimit)
}

object sortandlimit extends ToSortAndLimitOps
