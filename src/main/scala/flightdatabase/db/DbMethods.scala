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

  def getCityNames(maybeCountry: Option[String]): ConnectionIO[ApiResult[List[String]]] =
    maybeCountry match {
      case Some(country) =>
        // Get only cities from given country
        for {
          countryId <- (getIdsFragment("country") ++ whereNameFragment(country)).query[Int].unique
          cities <- (getNamesFragment("city") ++ fr"WHERE country_id = $countryId")
            .query[String]
            .to[List]
            .map(liftStringListToApiResult)
        } yield cities
      case _ =>
        // Get all cities in DB
        getStringList("city")
    }

  def getAirplaneNames(maybeManufacturer: Option[String]): ConnectionIO[ApiResult[List[String]]] =
    maybeManufacturer match {
      case Some(manufacturer) =>
        // Get only airplanes made by given manufacturer
        for {
          manufacturerId <- (getIdsFragment("manufacturer") ++ whereNameFragment(manufacturer))
            .query[Int]
            .unique
          airplanes <- (getNamesFragment("airplane") ++ fr"WHERE manufacturer_id = $manufacturerId")
            .query[String]
            .to[List]
            .map(liftStringListToApiResult)
        } yield airplanes
      case None =>
        // Get all airplanes in DB
        getStringList("airplane")
    }

  def getAirplanes(maybeManufacturer: Option[String]): ConnectionIO[ApiResult[List[Airplane]]] = {
    val allAirplanes =
      fr"SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km" ++
        fr"FROM airplane a INNER JOIN manufacturer m on a.manufacturer_id = m.id"

    val addManufacturer = maybeManufacturer.fold(Fragment.empty)(m => fr"WHERE m.name = $m")

    (allAirplanes ++ addManufacturer).query[Airplane].to[List].map(liftAirplaneListToApiResult)
  }

  def getLanguages: ConnectionIO[ApiResult[List[Language]]] =
    sql"SELECT id, name, iso2, iso3, original_name FROM language"
      .query[Language]
      .to[List]
      .map(liftLanguageListToApiResult)
}
