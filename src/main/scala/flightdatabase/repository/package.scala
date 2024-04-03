package flightdatabase

import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import flightdatabase.domain.FlightDbTable.Table
import flightdatabase.domain._
import flightdatabase.utils.implicits._

package object repository {

  // Helper methods to access DB
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
          strings <- id match {
            case Some(i) =>
              getNameWhereIdFragment(mainTable, s"${subTable}_id", i)
                .query[String]
                .to[List]
                .map(liftListToApiResult)
            case None => liftErrorToApiResult[List[String]](EntryNotFound).pure[ConnectionIO]
          }
        } yield strings

      case None =>
        // Get all strings in DB
        getStringList(mainTable)
    }

  def insertDbObject[O <: ModelBase](obj: O): ConnectionIO[ApiResult[Long]] =
    obj.sqlInsert.update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, CreatedValue(_)))

  // Fragment functions
  def getNamesFragment(table: Table): Fragment =
    fr"SELECT name FROM" ++ Fragment.const(table.toString)
  def getIdsFragment(table: Table): Fragment = fr"SELECT id FROM" ++ Fragment.const(table.toString)

  def whereNameFragment(name: String): Fragment = fr"WHERE name = $name"
  def whereIdFragment(id: Int): Fragment = fr"WHERE id = $id"

  def getIdWhereNameFragment(table: Table, name: String): Fragment =
    getIdsFragment(table) ++ whereNameFragment(name)

  def getNameWhereIdFragment(table: Table, idField: String, id: Long): Fragment =
    getNamesFragment(table) ++ fr"WHERE" ++ Fragment.const(idField) ++ fr"= $id"

// SQL state to ApiError conversion
  def sqlStateToApiError(state: SqlState): ApiError = state match {
    case sqlstate.class23.CHECK_VIOLATION    => EntryCheckFailed
    case sqlstate.class23.NOT_NULL_VIOLATION => EntryNullCheckFailed
    case sqlstate.class23.UNIQUE_VIOLATION   => EntryAlreadyExists
    case _                                   => UnknownError
  }

  // Lift to API Result
  def liftListToApiResult[A](list: List[A]): ApiResult[List[A]] =
    GotValue[List[A]](list).asRight[ApiError]

  def liftErrorToApiResult[A](error: ApiError): ApiResult[A] =
    error.asLeft[ApiOutput[A]]
}
