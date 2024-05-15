package flightdatabase.api.endpoints

import enumeratum._

sealed abstract class ResultOrder(override val entryName: String) extends EnumEntry

object ResultOrder extends Enum[ResultOrder] {
  val values = findValues

  case object Asc extends ResultOrder("asc")
  case object Desc extends ResultOrder("desc")

  implicit class StringOrderOps(private val s: String) extends AnyVal {
    def toOrder: Either[NoSuchMember[ResultOrder], ResultOrder] = withNameInsensitiveEither(s)
  }
}
