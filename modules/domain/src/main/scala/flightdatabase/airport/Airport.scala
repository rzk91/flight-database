package flightdatabase.airport

import flightdatabase.FlightDbTable.AIRPORT
import flightdatabase._
import io.circe.generic.extras.ConfiguredJsonCodec
import io.circe.generic.extras.JsonKey

@ConfiguredJsonCodec final case class Airport(
  id: Long,
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

object Airport {

  implicit val airportTableBase: TableBase[Airport] = TableBase.instance(
    AIRPORT,
    Map(
      "id"                       -> LongType,
      "name"                     -> StringType,
      "icao"                     -> StringType,
      "iata"                     -> StringType,
      "city_id"                  -> LongType,
      "number_of_runways"        -> IntType,
      "number_of_terminals"      -> IntType,
      "capacity"                 -> LongType,
      "international"            -> BooleanType,
      "junction"                 -> BooleanType,
      "latitude"                 -> BigDecimalType,
      "longitude"                -> BigDecimalType,
      "typical_taxi_out_minutes" -> IntType,
      "typical_taxi_in_minutes"  -> IntType
    )
  )

  def fromCreate(id: Long, model: AirportCreate): Airport =
    Airport(
      id,
      model.name,
      model.icao,
      model.iata,
      model.cityId,
      model.numRunways,
      model.numTerminals,
      model.capacity,
      model.international,
      model.junction,
      model.latitude,
      model.longitude,
      model.typicalTaxiOutMinutes,
      model.typicalTaxiInMinutes
    )

  def fromPatch(id: Long, patch: AirportPatch, airport: Airport): Airport =
    Airport(
      id,
      patch.name.getOrElse(airport.name),
      patch.icao.getOrElse(airport.icao),
      patch.iata.getOrElse(airport.iata),
      patch.cityId.getOrElse(airport.cityId),
      patch.numRunways.getOrElse(airport.numRunways),
      patch.numTerminals.getOrElse(airport.numTerminals),
      patch.capacity.getOrElse(airport.capacity),
      patch.international.getOrElse(airport.international),
      patch.junction.getOrElse(airport.junction),
      patch.latitude.getOrElse(airport.latitude),
      patch.longitude.getOrElse(airport.longitude),
      patch.typicalTaxiOutMinutes.getOrElse(airport.typicalTaxiOutMinutes),
      patch.typicalTaxiInMinutes.getOrElse(airport.typicalTaxiInMinutes)
    )
}
