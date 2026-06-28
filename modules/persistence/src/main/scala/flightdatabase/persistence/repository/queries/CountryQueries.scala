package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._
import flightdatabase.Operator
import flightdatabase.TableBase
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.country.Country
import flightdatabase.country.CountryCreate
import flightdatabase.persistence.syntax.sortandlimit._

private[repository] object CountryQueries {

  def countryExists(id: Long): Query0[Boolean] = idExistsQuery[Country](id)

  def selectAllCountries(sortAndLimit: ValidatedSortAndLimit): Query0[Country] =
    (selectAll ++ sortAndLimit.fragment).query[Country]

  def selectCountriesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Country] =
    (selectAll ++ whereFragment(s"country.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Country]

  def selectCountriesByExternal[ET: TableBase, EV: Put](
    fields: Nel[String],
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Country] = {
    selectAll ++ fr"WHERE" ++ multiFieldMembership[Country, ET, EV](
      fields,
      externalField,
      externalValues,
      operator
    ) ++ sortAndLimit.fragment
  }.query[Country]

  def insertCountry(model: CountryCreate): Update0 =
    sql"""INSERT INTO country 
         |       (name, iso2, iso3, country_code, domain_name, 
         |       main_language_id, secondary_language_id, tertiary_language_id, 
         |       currency_id, nationality)
         |   VALUES (
         |       ${model.name}, 
         |       ${model.iso2.toUpperCase},
         |       ${model.iso3.toUpperCase},
         |       ${model.countryCode},
         |       ${model.domainName},
         |       ${model.mainLanguageId},
         |       ${model.secondaryLanguageId},
         |       ${model.tertiaryLanguageId},
         |       ${model.currencyId},
         |       ${model.nationality}
         |   );
         | """.stripMargin.update

  def modifyCountry(model: Country): Update0 =
    sql"""
         | UPDATE country
         | SET
         |  name = ${model.name},
         |  iso2 = ${model.iso2.toUpperCase},
         |  iso3 = ${model.iso3.toUpperCase},
         |  country_code = ${model.countryCode},
         |  domain_name = ${model.domainName},
         |  main_language_id = ${model.mainLanguageId},
         |  secondary_language_id = ${model.secondaryLanguageId},
         |  tertiary_language_id = ${model.tertiaryLanguageId},
         |  currency_id = ${model.currencyId},
         |  nationality = ${model.nationality}
         | WHERE id = ${model.id}
         | """.stripMargin.update

  def deleteCountry(id: Long): Update0 = deleteWhereId[Country](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  country.id, country.name, country.iso2, country.iso3,
        |  country.country_code, country.domain_name,
        |  country.main_language_id, country.secondary_language_id, country.tertiary_language_id,
        |  country.currency_id, country.nationality
        |FROM country
      """.stripMargin
}
