package flightdatabase.domain.airline_route

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirlineRouteCreate(
  id: Option[Long],
  airlineAirplaneId: Long,
  @JsonKey("route_number") route: String,
  @JsonKey("start_airport_id") start: Long,
  @JsonKey("destination_airport_id") destination: Long
)

object AirlineRouteCreate {

  def apply(
    airlineAirplaneId: Long,
    route: String,
    start: Long,
    destination: Long
  ): AirlineRouteCreate =
    new AirlineRouteCreate(
      None,
      airlineAirplaneId,
      route,
      start,
      destination
    )
}
