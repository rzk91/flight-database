package flightdatabase.domain.airline_route

import flightdatabase.domain.FlightDbTable.AIRLINE_ROUTE
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirlineRoute(
  id: Long,
  airlineAirplaneId: Long,
  @JsonKey("route_number") route: String,
  @JsonKey("start_airport_id") start: Long,
  @JsonKey("destination_airport_id") destination: Long
)

object AirlineRoute {

  implicit val airlineRouteTableBase: TableBase[AirlineRoute] = TableBase.instance(
    AIRLINE_ROUTE,
    Map(
      "id"                     -> LongType,
      "airline_airplane_id"    -> LongType,
      "route_number"           -> StringType,
      "start_airport_id"       -> LongType,
      "destination_airport_id" -> LongType
    )
  )

  def fromCreate(id: Long, model: AirlineRouteCreate): AirlineRoute =
    AirlineRoute(
      id,
      model.airlineAirplaneId,
      model.route,
      model.start,
      model.destination
    )

  def fromPatch(id: Long, patch: AirlineRoutePatch, original: AirlineRoute): AirlineRoute =
    AirlineRoute(
      id,
      patch.airlineAirplaneId.getOrElse(original.airlineAirplaneId),
      patch.route.getOrElse(original.route),
      patch.start.getOrElse(original.start),
      patch.destination.getOrElse(original.destination)
    )
}
