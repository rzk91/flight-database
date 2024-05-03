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

  implicit val airlineTableBase: TableBase[Airline] =
    TableBase.instance(
      AIRLINE,
      Map(
        "id"         -> LongType,
        "name"       -> StringType,
        "iata"       -> StringType,
        "icao"       -> StringType,
        "call_sign"  -> StringType,
        "country_id" -> LongType
      )
    )

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
