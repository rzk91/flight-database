package flightdatabase.domain.city

import flightdatabase.domain.FlightDbTable.CITY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class City(
  id: Long,
  name: String,
  countryId: Long,
  capital: Boolean,
  population: Long,
  latitude: BigDecimal,
  longitude: BigDecimal,
  timezone: String
)

object City {

  implicit val cityTableBase: TableBase[City] = TableBase.instance(
    CITY,
    Set("id", "name", "country_id", "capital", "population", "latitude", "longitude", "timezone")
  )

  def fromCreate(id: Long, model: CityCreate): City =
    City(
      id,
      model.name,
      model.countryId,
      model.capital,
      model.population,
      model.latitude,
      model.longitude,
      model.timezone
    )

  def fromPatch(id: Long, patch: CityPatch, original: City): City =
    City(
      id,
      patch.name.getOrElse(original.name),
      patch.countryId.getOrElse(original.countryId),
      patch.capital.getOrElse(original.capital),
      patch.population.getOrElse(original.population),
      patch.latitude.getOrElse(original.latitude),
      patch.longitude.getOrElse(original.longitude),
      patch.timezone.getOrElse(original.timezone)
    )
}
