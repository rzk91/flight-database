package flightdatabase.domain.airport

import flightdatabase.domain.FlightDbTable.AIRPORT
import flightdatabase.domain._
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
  junction: Boolean
)

object Airport {

  implicit val airportTableBase: TableBase[Airport] = TableBase.instance(AIRPORT)

  def fromCreate(model: AirportCreate): Option[Airport] =
    model.id.map { id =>
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
        model.junction
      )
    }

  def fromCreateUnsafe(model: AirportCreate): Airport =
    fromCreate(model).get

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
      patch.junction.getOrElse(airport.junction)
    )
}
