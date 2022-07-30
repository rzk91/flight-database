package objects

import io.circe.generic.extras._
import CirceClass._

@ConfiguredJsonCodec final case class Airplane(
  name: String,
  manufacturerId: String,
  capacity: Int,
  maxRangeKm: Int
) extends CirceClass {
  def sqlInsert: String = ???
}
