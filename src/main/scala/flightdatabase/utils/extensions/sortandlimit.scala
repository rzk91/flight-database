package flightdatabase.utils.extensions

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain.{ResultOrder, ValidatedSortAndLimit}

final class ValidatedSortAndLimitOps(private val self: ValidatedSortAndLimit) extends AnyVal {

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

trait ToValidatedSortAndLimitOps {
  @inline implicit def toValidatedSortAndLimitOps(
    self: ValidatedSortAndLimit
  ): ValidatedSortAndLimitOps = new ValidatedSortAndLimitOps(self)
}

object sortandlimit extends ToValidatedSortAndLimitOps
