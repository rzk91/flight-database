package flightdatabase.domain.city

import flightdatabase.domain.FlightDbTable.CITY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CityModel(
  id: Option[Long],
  name: String,
  countryId: Int,
  capital: Boolean,
  population: Int,
  latitude: Double,
  longitude: Double
)

object CityModel {
  implicit val cityModelTable: TableBase[CityModel] = TableBase.instance(CITY)
}
