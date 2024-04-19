package flightdatabase.domain.fleet_route

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetRouteCreate(
  id: Option[Long],
  fleetAirplaneId: Long,
  @JsonKey("route_number") route: String,
  @JsonKey("start_airport_id") start: Long,
  @JsonKey("destination_airport_id") destination: Long
)
