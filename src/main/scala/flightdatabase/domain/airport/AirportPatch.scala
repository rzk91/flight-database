package flightdatabase.domain.airport

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import io.circe.generic.extras.JsonKey

@ConfiguredJsonCodec final case class AirportPatch(
  name: Option[String],
  icao: Option[String],
  iata: Option[String],
  cityId: Option[Long],
  @JsonKey("number_of_runways") numRunways: Option[Int],
  @JsonKey("number_of_terminals") numTerminals: Option[Int],
  capacity: Option[Long],
  international: Option[Boolean],
  junction: Option[Boolean]
)
