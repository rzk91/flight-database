package objects

import io.circe.generic.extras._
import objects.CirceClass._

@ConfiguredJsonCodec final case class Country(
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
) extends CirceClass {
  def sqlInsert: String =
    s"""INSERT INTO country 
    |       (name, iso2, iso3, country_code, domain_name, 
    |       main_language, secondary_language, tertiary_language, 
    |       currency, nationality)
    |   VALUES (
    |       '$name', '$iso2', '$iso3', $countryCode,
    |       ${insertWithNull(domainName)},
    |       ${selectIdStmt("language", Some(mainLanguage))},
    |       ${selectIdStmt("language", secondaryLanguage)},
    |       ${selectIdStmt("language", tertiaryLanguage)},
    |       ${selectIdStmt("currency", Some(currency))},
    |       '$nationality'
    |   );
    | """.stripMargin
}
