package flightdatabase

import enumeratum._

sealed abstract class ResultOrder(override val entryName: String) extends EnumEntry

object ResultOrder extends Enum[ResultOrder] {
  val values = findValues

  case object Ascending extends ResultOrder("ASC")
  case object Descending extends ResultOrder("DESC")
}
