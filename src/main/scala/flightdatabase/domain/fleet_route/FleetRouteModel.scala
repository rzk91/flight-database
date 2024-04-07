package flightdatabase.domain.fleet_route

import flightdatabase.domain.FlightDbTable.FLEET_ROUTE
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetRouteModel(
  id: Option[Long],
  fleetAirplaneId: Long,
  @JsonKey("route_number") route: String,
  @JsonKey("start_airport_id") start: Long,
  @JsonKey("destination_airport_id") destination: Long
)

object FleetRouteModel {
  implicit val fleetRouteModelTable: TableBase[FleetRouteModel] = TableBase.instance(FLEET_ROUTE)
}
