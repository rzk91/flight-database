package flightdatabase.domain.airline

import flightdatabase.domain.FlightDbTable.AIRLINE
import flightdatabase.domain._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Airline(
  id: Long,
  name: String,
  iata: String,
  icao: String,
  callSign: String,
  countryId: Long
)

object Airline {
  implicit val airlineTableBase: TableBase[Airline] = TableBase.instance(AIRLINE)

  def fromCreate(id: Long, model: AirlineCreate): Airline =
    Airline(
      id,
      model.name,
      model.iata,
      model.icao,
      model.callSign,
      model.countryId
    )

  def fromPatch(id: Long, patch: AirlinePatch, original: Airline): Airline =
    Airline(
      id,
      patch.name.getOrElse(original.name),
      patch.iata.getOrElse(original.iata),
      patch.icao.getOrElse(original.icao),
      patch.callSign.getOrElse(original.callSign),
      patch.countryId.getOrElse(original.countryId)
    )
}
