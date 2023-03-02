package flightdatabase.db

import doobie._
import doobie.implicits._
import doobie.postgres._
import flightdatabase.model.objects._
import java.sql.SQLException
import flightdatabase.model._
import flightdatabase.utils.CollectionsHelper._

object DbMethods {

  def getStringList(field: String): ConnectionIO[ApiResult[List[String]]] =
    getNamesFragment(field)
      .query[String]
      .to[List]
      .map(liftStringListToApiResult)

  def insertDbObject[O <: DbObject](
    obj: O
  )(implicit updateId: (Long, O) => O): ConnectionIO[Either[ApiError, O]] =
    obj.sqlInsert.update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, updateId(_, obj)))

  def insertLanguage(lang: Language): ConnectionIO[ApiResult[Language]] =
    insertDbObject[Language](lang).map(_.map(CreatedLanguage(_)))

  def whereCountryIdFragment(countryId: Int): Fragment = fr"WHERE country_id = $countryId"

  def getCityNames(maybeCountry: Option[String]): ConnectionIO[ApiResult[List[String]]] =
    maybeCountry match {
      case Some(country) =>
        // Get only cities from given country
        for {
          countryId <- (getIdsFragment("country") ++ whereNameFragment(country)).query[Int].unique
          cities <- (getNamesFragment("city") ++ whereCountryIdFragment(countryId))
            .query[String]
            .to[List]
            .map(liftStringListToApiResult)
        } yield cities
      case _ =>
        // Get all cities in DB
        getStringList("city")
    }

  def getLanguages: ConnectionIO[ApiResult[List[Language]]] =
    sql"SELECT id, name, iso2, iso3, original_name FROM language"
      .query[Language]
      .to[List]
      .map(liftLanguageListToApiResult)
}
