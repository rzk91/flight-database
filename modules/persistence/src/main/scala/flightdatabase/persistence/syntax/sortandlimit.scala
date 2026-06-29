package flightdatabase.persistence.syntax

import flightdatabase.ResultOrder
import flightdatabase.ValidatedSortAndLimit
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.syntax.string._

final class SortAndLimitOps(private val self: ValidatedSortAndLimit) extends AnyVal {

  def fragment: Fragment = {
    val sort = self.sortBy.fold(fr"") { s =>
      val ord = self.order.getOrElse(ResultOrder.Ascending).entryName
      fr"ORDER BY" ++ Fragment.const(s) ++ Fragment.const(ord) ++
      fr"," ++ Fragment.const("id") ++ Fragment.const(ord) // Make sorting stable
    }
    val lim = self.limit.filter(_ > 0).fold(fr"")(l => fr"LIMIT $l")
    val off = self.offset.filter(_ > 0).fold(fr"")(o => fr"OFFSET $o")
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
