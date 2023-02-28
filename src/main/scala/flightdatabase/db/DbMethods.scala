package flightdatabase.db

import doobie._
import doobie.implicits._
import flightdatabase.model.objects._
import java.sql.SQLException

object DbMethods {

  def whereCountryIdFragment(countryId: Int): Fragment = fr"WHERE country_id = $countryId"

  def getCountryNames: ConnectionIO[List[String]] =
    getNamesFragment("country").query[String].to[List]

  def getCityNames(maybeCountry: Option[String]): ConnectionIO[List[String]] = maybeCountry match {
    case Some(country) =>
      // Get only cities from given country
      for {
        countryId <- (getIdsFragment("country") ++ whereNameFragment(country)).query[Int].unique
        cities <- (getNamesFragment("city") ++ whereCountryIdFragment(countryId))
          .query[String]
          .to[List]
      } yield cities
    case _ =>
      // Get all cities in DB
      getNamesFragment("city").query[String].to[List]
  }

  def getLanguageNames: ConnectionIO[List[String]] =
    getNamesFragment("language").query[String].to[List]

  def getLanguages: ConnectionIO[List[Language]] =
    sql"SELECT id, name, iso2, iso3, original_name FROM language".query[Language].to[List]

  def insertLanguage(language: Language): ConnectionIO[Either[SQLException, Language]] =
    language.sqlInsert.update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSql
      .map(_.map(l => language.copy(id = Some(l))))
}
