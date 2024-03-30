package flightdatabase.db

import cats.implicits._
import doobie._
import doobie.implicits._
import flightdatabase.api._
import flightdatabase.model.FlightDbTable._
import flightdatabase.model._
import flightdatabase.model.objects._
import flightdatabase.utils.implicits._

object DbMethods {

  // Move helper methods to package object of repository
  def getStringList(table: Table): ConnectionIO[ApiResult[List[String]]] =
    getNamesFragment(table)
      .query[String]
      .to[List]
      .map(liftListToApiResult)

  def getStringListBy(
    mainTable: Table,
    subTable: Table,
    subTableValue: Option[String]
  ): ConnectionIO[ApiResult[List[String]]] =
    subTableValue match {
      case Some(value) =>
        // Get only strings based on given value
        for {
          id <- getIdWhereNameFragment(subTable, value).query[Int].option
          strings <- {
            id match {
              case Some(i) =>
                getNameWhereIdFragment(mainTable, s"${subTable}_id", i)
                  .query[String]
                  .to[List]
              case None => List.empty[String].pure[ConnectionIO]
            }
          }.map(liftListToApiResult)
        } yield strings

      case None =>
        // Get all strings in DB
        getStringList(mainTable)
    }

  def insertDbObject[O <: FlightDbBase](
    obj: O
  )(implicit updateId: (Long, O) => O): ConnectionIO[ApiResult[O]] =
    obj.sqlInsert.update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, id => CreatedValue(updateId(id, obj))))

  def insertLanguage(lang: Language): ConnectionIO[ApiResult[Language]] =
    insertDbObject[Language](lang)

  def getAirplanes(maybeManufacturer: Option[String]): ConnectionIO[ApiResult[List[Airplane]]] = {
    val allAirplanes =
      fr"SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km" ++
        fr"FROM airplane a INNER JOIN manufacturer m on a.manufacturer_id = m.id"

    val addManufacturer = maybeManufacturer.fold(Fragment.empty)(m => fr"WHERE m.name = $m")

    (allAirplanes ++ addManufacturer).query[Airplane].to[List].map(liftListToApiResult)
  }

  // TODO: Move this to LanguageRepository
  def getLanguages: ConnectionIO[ApiResult[List[Language]]] =
    sql"SELECT id, name, iso2, iso3, original_name FROM language"
      .query[Language]
      .to[List]
      .map(liftListToApiResult)


  // TODO: Move this to CurrencyRepository
  def getCurrencies: ConnectionIO[ApiResult[List[Currency]]] =
    sql"SELECT id, name, iso, symbol FROM currency"
      .query[Currency]
      .to[List]
      .map(liftListToApiResult)
}
