package flightdatabase

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres._
import doobie.util.log.{ExecFailure, ProcessingFailure, Success}
import flightdatabase.config.Configuration._
import flightdatabase.model._
import flightdatabase.model.objects._

package object db extends LazyLogging {

  implicit val transactor: Resource[IO, HikariTransactor[IO]] = DbInitiation.transactor(dbConfig)

  // Implicit logging
  implicit val logHandler: LogHandler = LogHandler {
    case Success(s, a, e1, e2) =>
      logger.debug(s"""Successful Statement Execution:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
          """.stripMargin)

    case ProcessingFailure(s, a, e1, e2, t) =>
      logger.whenDebugEnabled {
        logger.warn(s"""Failed Resultset Processing:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
            |   failure = ${t.getMessage}
          """.stripMargin)
      }

    case ExecFailure(s, a, e1, t) =>
      logger.whenDebugEnabled {
        logger.error(s"""Failed Statement Execution:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec (failed)
            |   failure = ${t.getMessage}
          """.stripMargin)
      }
  }

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
  def liftStringListToApiResult(list: List[String]): ApiResult[List[String]] =
    Right(GotStringList(list)).withLeft[ApiError]

  def liftLanguageListToApiResult(list: List[Language]): ApiResult[List[Language]] =
    Right(GotLanguageList(list)).withLeft[ApiError]

  def liftAirplaneListToApiResult(list: List[Airplane]): ApiResult[List[Airplane]] =
    Right(GotAirplaneList(list)).withLeft[ApiError]
}
