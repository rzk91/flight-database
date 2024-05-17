package flightdatabase.domain

import enumeratum._

sealed abstract class ResultOrder(override val entryName: String) extends EnumEntry

object ResultOrder extends Enum[ResultOrder] {
  val values = findValues

  case object Ascending extends ResultOrder("ASC")
  case object Descending extends ResultOrder("DESC")

  implicit class StringOrderOps(private val s: String) extends AnyVal {
    def toOrder: Either[NoSuchMember[ResultOrder], ResultOrder] = withNameInsensitiveEither(s)
  }
}
