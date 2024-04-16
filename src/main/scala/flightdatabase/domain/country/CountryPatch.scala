package flightdatabase.domain.country

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CountryPatch(
  name: Option[String],
  iso2: Option[String],
  iso3: Option[String],
  countryCode: Option[Int],
  domainName: Option[String],
  mainLanguageId: Option[Long],
  secondaryLanguageId: Option[Long],
  tertiaryLanguageId: Option[Long],
  currencyId: Option[Long],
  nationality: Option[String]
)
