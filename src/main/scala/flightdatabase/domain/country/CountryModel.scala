package flightdatabase.domain.country

import flightdatabase.domain.FlightDbTable.COUNTRY
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec final case class CountryModel(
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

object CountryModel {
  implicit val countryModelTable: TableBase[CountryModel] = TableBase.instance(COUNTRY)
}
