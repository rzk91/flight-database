package flightdatabase.airline_airplane

import flightdatabase._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineAirplanePatch(
  airlineId: Option[Long] = None,
  airplaneId: Option[Long] = None
)
