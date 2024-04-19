package flightdatabase.domain.fleet_airplane

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class FleetAirplaneCreate(
  id: Option[Long],
  fleetId: Long,
  airplaneId: Long
)
