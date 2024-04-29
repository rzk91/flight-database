package flightdatabase.domain.airline_airplane

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineAirplanePatch(
  airlineId: Option[Long] = None,
  airplaneId: Option[Long] = None
)
