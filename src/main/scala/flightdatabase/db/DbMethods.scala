package flightdatabase.db

import doobie._
import doobie.implicits._
import flightdatabase.model._
import flightdatabase.model.objects._
import flightdatabase.utils.CollectionsHelper._

object DbMethods {

  def getStringList(table: String): ConnectionIO[ApiResult[List[String]]] =
    getNamesFragment(table)
      .query[String]
      .to[List]
      .map(liftListToApiResult)

  def getStringListBy(
    toTable: String,
    fromTable: String,
    fromTableValue: Option[String]
  ): ConnectionIO[ApiResult[List[String]]] =
    fromTableValue match {
      case Some(value) =>
        // Get only strings based on given value
        for {
          id <- getIdWhereNameFragment(fromTable, value).query[Int].unique
          strings <- getNameWhereIdFragment(toTable, s"${fromTable}_id", id)
            .query[String]
            .to[List]
            .map(liftListToApiResult)
        } yield strings

      case None =>
        // Get all strings in DB
        getStringList(toTable)
    }

  def insertDbObject[O <: FlightDbBase](
    obj: O
  )(implicit updateId: (Long, O) => O): ConnectionIO[ApiResult[O]] =
    obj.sqlInsert.update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, id => CreatedValue(updateId(id, obj))))

  def insertLanguage(lang: Language): ConnectionIO[ApiResult[Language]] = insertDbObject[Language](lang)

  def getAirplanes(maybeManufacturer: Option[String]): ConnectionIO[ApiResult[List[Airplane]]] = {
    val allAirplanes =
      fr"SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km" ++
        fr"FROM airplane a INNER JOIN manufacturer m on a.manufacturer_id = m.id"

    val addManufacturer = maybeManufacturer.fold(Fragment.empty)(m => fr"WHERE m.name = $m")

    (allAirplanes ++ addManufacturer).query[Airplane].to[List].map(liftListToApiResult)
  }

  def getLanguages: ConnectionIO[ApiResult[List[Language]]] =
    sql"SELECT id, name, iso2, iso3, original_name FROM language"
      .query[Language]
      .to[List]
      .map(liftListToApiResult)

  def getCurrencies: ConnectionIO[ApiResult[List[Currency]]] =
    sql"SELECT id, name, iso, symbol FROM currency"
      .query[Currency]
      .to[List]
      .map(liftListToApiResult)
}
