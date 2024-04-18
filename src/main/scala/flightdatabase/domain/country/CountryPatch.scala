package flightdatabase.domain.country

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CountryPatch(
  name: Option[String] = None,
  iso2: Option[String] = None,
  iso3: Option[String] = None,
  countryCode: Option[Int] = None,
  domainName: Option[String] = None,
  mainLanguageId: Option[Long] = None,
  secondaryLanguageId: Option[Long] = None,
  tertiaryLanguageId: Option[Long] = None,
  currencyId: Option[Long] = None,
  nationality: Option[String] = None
)
