package flightdatabase.airport

import flightdatabase._
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
  junction: Boolean,
  latitude: BigDecimal,
  longitude: BigDecimal,
  typicalTaxiOutMinutes: Int,
  typicalTaxiInMinutes: Int
)

object AirportCreate {

  def apply(
    name: String,
    icao: String,
    iata: String,
    cityId: Long,
    numRunways: Int,
    numTerminals: Int,
    capacity: Long,
    international: Boolean,
    junction: Boolean,
    latitude: BigDecimal,
    longitude: BigDecimal,
    typicalTaxiOutMinutes: Int,
    typicalTaxiInMinutes: Int
  ): AirportCreate =
    new AirportCreate(
      None,
      name,
      icao,
      iata,
      cityId,
      numRunways,
      numTerminals,
      capacity,
      international,
      junction,
      latitude,
      longitude,
      typicalTaxiOutMinutes,
      typicalTaxiInMinutes
    )
}
