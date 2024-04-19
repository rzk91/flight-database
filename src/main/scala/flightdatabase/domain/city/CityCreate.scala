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
