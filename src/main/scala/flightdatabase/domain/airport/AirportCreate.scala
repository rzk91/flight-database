package flightdatabase.domain.airport

import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirportCreate(
  id: Option[Long],
  name: String,
  icao: String,
  iata: String,
  cityId: Long,
  @JsonKey("number_of_runways") numRunways: Int,
  @JsonKey("number_of_terminals") numTerminals: Int,
  capacity: Long,
  international: Boolean,
  junction: Boolean
)
