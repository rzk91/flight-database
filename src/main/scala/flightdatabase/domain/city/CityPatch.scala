package flightdatabase.domain.city

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CityPatch(
  name: Option[String] = None,
  countryId: Option[Long] = None,
  capital: Option[Boolean] = None,
  population: Option[Long] = None,
  latitude: Option[BigDecimal] = None,
  longitude: Option[BigDecimal] = None,
  timezone: Option[String] = None
)
