package flightdatabase.domain.country

import flightdatabase.domain.FlightDbTable.COUNTRY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class Country(
  id: Long,
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

object Country {

  implicit val countryTableBase: TableBase[Country] = TableBase.instance(
    COUNTRY,
    Map(
      "id"                    -> LongType,
      "name"                  -> StringType,
      "iso2"                  -> StringType,
      "iso3"                  -> StringType,
      "country_code"          -> IntType,
      "domain_name"           -> StringType,
      "main_language_id"      -> LongType,
      "secondary_language_id" -> LongType,
      "tertiary_language_id"  -> LongType,
      "currency_id"           -> LongType,
      "nationality"           -> StringType
    )
  )

  def fromCreate(id: Long, model: CountryCreate): Country =
    Country(
      id = id,
      name = model.name,
      iso2 = model.iso2,
      iso3 = model.iso3,
      countryCode = model.countryCode,
      domainName = model.domainName,
      mainLanguageId = model.mainLanguageId,
      secondaryLanguageId = model.secondaryLanguageId,
      tertiaryLanguageId = model.tertiaryLanguageId,
      currencyId = model.currencyId,
      nationality = model.nationality
    )

  def fromPatch(id: Long, patch: CountryPatch, original: Country): Country =
    Country(
      id = id,
      name = patch.name.getOrElse(original.name),
      iso2 = patch.iso2.getOrElse(original.iso2),
      iso3 = patch.iso3.getOrElse(original.iso3),
      countryCode = patch.countryCode.getOrElse(original.countryCode),
      domainName = patch.domainName.orElse(original.domainName),
      mainLanguageId = patch.mainLanguageId.getOrElse(original.mainLanguageId),
      secondaryLanguageId = patch.secondaryLanguageId.orElse(original.secondaryLanguageId),
      tertiaryLanguageId = patch.tertiaryLanguageId.orElse(original.tertiaryLanguageId),
      currencyId = patch.currencyId.getOrElse(original.currencyId),
      nationality = patch.nationality.getOrElse(original.nationality)
    )

}
