package flightdatabase.domain.airport

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import io.circe.generic.extras.JsonKey

@ConfiguredJsonCodec final case class AirportPatch(
  name: Option[String] = None,
  icao: Option[String] = None,
  iata: Option[String] = None,
  cityId: Option[Long] = None,
  @JsonKey("number_of_runways") numRunways: Option[Int] = None,
  @JsonKey("number_of_terminals") numTerminals: Option[Int] = None,
  capacity: Option[Long] = None,
  international: Option[Boolean] = None,
  junction: Option[Boolean] = None
)
