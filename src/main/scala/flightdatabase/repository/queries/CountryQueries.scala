package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits.toSqlInterpolator
import flightdatabase.domain.country.CountryModel

private[repository] object CountryQueries {

  def selectAllCountries: Query0[CountryModel] = selectAll.query[CountryModel]

  def insertCountry(model: CountryModel): Update0 =
    sql"""INSERT INTO country 
         |       (name, iso2, iso3, country_code, domain_name, 
         |       main_language_id, secondary_language_id, tertiary_language_id, 
         |       currency_id, nationality)
         |   VALUES (
         |       ${model.name}, 
         |       ${model.iso2}, 
         |       ${model.iso3}, 
         |       ${model.countryCode},
         |       ${model.domainName},
         |       ${model.mainLanguageId},
         |       ${model.secondaryLanguageId},
         |       ${model.tertiaryLanguageId},
         |       ${model.currencyId},
         |       ${model.nationality}
         |   );
         | """.stripMargin.update

  def deleteCountry(id: Long): Update0 = deleteWhereId[CountryModel](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  id, name, iso2, iso3, country_code, domain_name,
        |  main_language_id, secondary_language_id, tertiary_language_id,
        |  currency_id, nationality
        |FROM country
      """.stripMargin
}
