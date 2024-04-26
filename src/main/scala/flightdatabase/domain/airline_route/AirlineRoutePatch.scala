package flightdatabase.domain.airline_route

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirlineRoutePatch(
  airlineAirplaneId: Option[Long] = None,
  @JsonKey("route_number") route: Option[String] = None,
  @JsonKey("start_airport_id") start: Option[Long] = None,
  @JsonKey("destination_airport_id") destination: Option[Long] = None
)
