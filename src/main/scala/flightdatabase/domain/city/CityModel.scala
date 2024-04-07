package flightdatabase.domain.city

import flightdatabase.domain.FlightDbTable.CITY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CityModel(
  id: Option[Long],
  name: String,
  countryId: Long,
  capital: Boolean,
  population: Long,
  latitude: BigDecimal,
  longitude: BigDecimal,
  timezone: String
)

object CityModel {
  implicit val cityModelTable: TableBase[CityModel] = TableBase.instance(CITY)
}
