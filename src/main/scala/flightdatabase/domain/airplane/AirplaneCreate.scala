package flightdatabase.domain.airplane

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirplaneCreate(
  id: Option[Long],
  name: String,
  manufacturerId: Long,
  capacity: Int,
  maxRangeInKm: Int
)

object AirplaneCreate {

  def apply(name: String, manufacturerId: Long, capacity: Int, maxRangeInKm: Int): AirplaneCreate =
    new AirplaneCreate(None, name, manufacturerId, capacity, maxRangeInKm)
}
