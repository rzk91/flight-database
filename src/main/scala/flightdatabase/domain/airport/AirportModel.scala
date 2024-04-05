package flightdatabase.domain.airport

import flightdatabase.domain.FlightDbTable.AIRPORT
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class AirportModel(
  id: Option[Long],
  name: String,
  icao: String,
  iata: String,
  cityId: Int,
  countryId: Int,
  @JsonKey("number_of_runways") numRunways: Int,
  @JsonKey("number_of_terminals") numTerminals: Int,
  capacity: Int,
  international: Boolean,
  junction: Boolean
)

object AirportModel {
  implicit val airportModelTable: TableBase[AirportModel] = TableBase.instance(AIRPORT)
}