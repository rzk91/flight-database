package flightdatabase.domain.fleet_route

import flightdatabase.domain.FlightDbTable.FLEET_ROUTE
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetRoute(
  id: Long,
  fleetAirplaneId: Long,
  @JsonKey("route_number") route: String,
  @JsonKey("start_airport_id") start: Long,
  @JsonKey("destination_airport_id") destination: Long
)

object FleetRoute {
  implicit val fleetRouteTableBase: TableBase[FleetRoute] = TableBase.instance(FLEET_ROUTE)

  def fromCreate(model: FleetRouteCreate): Option[FleetRoute] =
    model.id.map { id =>
      FleetRoute(
        id,
        model.fleetAirplaneId,
        model.route,
        model.start,
        model.destination
      )
    }

  def fromCreateUnsafe(model: FleetRouteCreate): FleetRoute =
    fromCreate(model).get

  def fromPatch(id: Long, patch: FleetRoutePatch, original: FleetRoute): FleetRoute =
    FleetRoute(
      id,
      patch.fleetAirplaneId.getOrElse(original.fleetAirplaneId),
      patch.route.getOrElse(original.route),
      patch.start.getOrElse(original.start),
      patch.destination.getOrElse(original.destination)
    )
}
