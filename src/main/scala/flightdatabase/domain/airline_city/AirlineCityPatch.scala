package flightdatabase.domain.airline_city

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineCityPatch(
  airlineId: Option[Long] = None,
  cityId: Option[Long] = None
)
