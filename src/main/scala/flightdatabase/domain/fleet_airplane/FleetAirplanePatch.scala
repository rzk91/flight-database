package flightdatabase.domain.fleet_airplane

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetAirplanePatch(
  fleetId: Option[Long] = None,
  airplaneId: Option[Long] = None
)
