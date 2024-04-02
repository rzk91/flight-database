package flightdatabase.domain.country

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class CountryModel(
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
) extends ModelBase {
  def uri: Uri = ???

  override def updateId(newId: Long): CountryModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
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
