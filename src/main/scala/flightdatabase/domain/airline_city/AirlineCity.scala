package flightdatabase.domain.airline_city

import flightdatabase.domain.FlightDbTable.AIRLINE_CITY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class AirlineCity(
  id: Long,
  airlineId: Long,
  cityId: Long
)

object AirlineCity {

  implicit val airlineCityTableBase: TableBase[AirlineCity] =
    TableBase.instance(AIRLINE_CITY, Set("id", "airline_id", "city_id"))

  def fromCreate(id: Long, model: AirlineCityCreate): AirlineCity =
    AirlineCity(
      id,
      model.airlineId,
      model.cityId
    )

  def fromPatch(id: Long, patch: AirlineCityPatch, original: AirlineCity): AirlineCity =
    AirlineCity(
      id,
      patch.airlineId.getOrElse(original.airlineId),
      patch.cityId.getOrElse(original.cityId)
    )
}
