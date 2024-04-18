package flightdatabase.domain.fleet_route

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetRoutePatch(
  fleetAirplaneId: Option[Long] = None,
  @JsonKey("route_number") route: Option[String] = None,
  @JsonKey("start_airport_id") start: Option[Long] = None,
  @JsonKey("destination_airport_id") destination: Option[Long] = None
)
