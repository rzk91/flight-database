package flightdatabase.domain.city

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CityPatch(
  name: Option[String],
  countryId: Option[Long],
  capital: Option[Boolean],
  population: Option[Long],
  latitude: Option[BigDecimal],
  longitude: Option[BigDecimal],
  timezone: Option[String]
)
