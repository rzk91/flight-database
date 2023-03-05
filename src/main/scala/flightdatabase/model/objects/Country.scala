package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class Country(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  countryCode: Int,
  domainName: Option[String],
  mainLanguage: String,
  secondaryLanguage: Option[String],
  tertiaryLanguage: Option[String],
  currency: String,
  nationality: String
) extends FlightDbBase {

  def sqlInsert: Fragment =
    sql"""INSERT INTO country 
    |       (name, iso2, iso3, country_code, domain_name, 
    |       main_language_id, secondary_language_id, tertiary_language_id, 
    |       currency_id, nationality)
    |   VALUES (
    |       $name, $iso2, $iso3, $countryCode,
    |       $domainName,
    |       ${selectIdStmt("language", Some(mainLanguage))},
    |       ${selectIdStmt("language", secondaryLanguage)},
    |       ${selectIdStmt("language", tertiaryLanguage)},
    |       ${selectIdStmt("currency", Some(currency), keyField = "iso")},
    |       $nationality
    |   );
    | """.stripMargin
}
