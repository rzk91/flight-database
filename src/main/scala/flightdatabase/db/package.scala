package flightdatabase

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres._
import flightdatabase.config.Configuration._
import flightdatabase.model._
import flightdatabase.model.objects._

package object db {

  implicit val transactor: Resource[IO, HikariTransactor[IO]] = DbInitiation.transactor(dbConfig)

  // Implicit logging
  implicit val logHandler: LogHandler =
    if (environment.debugLogging) LogHandler.jdkLogHandler else LogHandler.nop

  // Fragment functions
  def getNamesFragment(table: String): Fragment = fr"SELECT name FROM" ++ Fragment.const(table)
  def getIdsFragment(table: String): Fragment = fr"SELECT id FROM" ++ Fragment.const(table)

  def whereNameFragment(name: String): Fragment = fr"WHERE name = $name"
  def whereIdFragment(id: Int): Fragment = fr"WHERE id = $id"

  def getIdWhereNameFragment(table: String, name: String): Fragment =
    getIdsFragment(table) ++ whereNameFragment(name)
  def getNameWhereIdFragment(table: String, idField: String, id: Long): Fragment =
    getNamesFragment(table) ++ fr"WHERE" ++ Fragment.const(idField) ++ fr"= $id"

  // SQL state to ApiError conversion
  def sqlStateToApiError(state: SqlState): ApiError = state match {
    case sqlstate.class23.CHECK_VIOLATION    => EntryCheckFailed
    case sqlstate.class23.NOT_NULL_VIOLATION => EntryNullCheckFailed
    case sqlstate.class23.UNIQUE_VIOLATION   => EntryAlreadyExists
    case _                                   => UnknownError
  }

  // Lift to API Result
  def liftStringListToApiResult(list: List[String]): ApiResult[List[String]] = Right(GotStringList(list)).withLeft[ApiError]
  def liftLanguageListToApiResult(list: List[Language]): ApiResult[List[Language]] = Right(GotLanguageList(list)).withLeft[ApiError]
  def liftAirplaneListToApiResult(list: List[Airplane]): ApiResult[List[Airplane]] = Right(GotAirplaneList(list)).withLeft[ApiError]
}
