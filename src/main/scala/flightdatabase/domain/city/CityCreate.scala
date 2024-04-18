package flightdatabase.domain.city

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CityCreate(
  id: Option[Long],
  name: String,
  countryId: Long,
  capital: Boolean,
  population: Long,
  latitude: BigDecimal,
  longitude: BigDecimal,
  timezone: String
)

object CityCreate {

  def apply(
    name: String,
    countryId: Long,
    capital: Boolean,
    population: Long,
    latitude: BigDecimal,
    longitude: BigDecimal,
    timezone: String
  ): CityCreate =
    new CityCreate(
      None,
      name,
      countryId,
      capital,
      population,
      latitude,
      longitude,
      timezone
    )
}
