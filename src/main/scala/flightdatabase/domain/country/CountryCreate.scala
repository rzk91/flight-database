package flightdatabase.domain.country

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CountryCreate(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  countryCode: Int,
  domainName: Option[String],
  mainLanguageId: Long,
  secondaryLanguageId: Option[Long],
  tertiaryLanguageId: Option[Long],
  currencyId: Long,
  nationality: String
)

object CountryCreate {

  def apply(
    name: String,
    iso2: String,
    iso3: String,
    countryCode: Int,
    domainName: Option[String],
    mainLanguageId: Long,
    secondaryLanguageId: Option[Long],
    tertiaryLanguageId: Option[Long],
    currencyId: Long,
    nationality: String
  ): CountryCreate =
    new CountryCreate(
      None,
      name,
      iso2,
      iso3,
      countryCode,
      domainName,
      mainLanguageId,
      secondaryLanguageId,
      tertiaryLanguageId,
      currencyId,
      nationality
    )
}
