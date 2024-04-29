package flightdatabase.domain.airline_city

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineCityCreate(
  id: Option[Long],
  airlineId: Long,
  cityId: Long
)

object AirlineCityCreate {

  def apply(airlineId: Long, cityId: Long): AirlineCityCreate =
    new AirlineCityCreate(None, airlineId, cityId)
}
