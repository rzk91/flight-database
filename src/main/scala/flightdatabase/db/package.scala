package flightdatabase

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres._
import doobie.util.log.ExecFailure
import doobie.util.log.ProcessingFailure
import doobie.util.log.Success
import flightdatabase.api._
import flightdatabase.config.Configuration.DatabaseConfig
import flightdatabase.model.FlightDbTable._
import flightdatabase.model._

package object db extends LazyLogging {

  // Implicit logging
  implicit val logHandler: LogHandler = LogHandler {
    case Success(s, a, e1, e2) =>
      logger.debug(s"""Successful statement execution:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
          """.stripMargin)

    case ProcessingFailure(s, a, e1, e2, t) =>
      logger.whenDebugEnabled {
        logger.warn(s"""Failed result set processing:
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
        logger.error(s"""Failed statement execution:
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
  def getNamesFragment(table: Table): Fragment = fr"SELECT name FROM" ++ Fragment.const(table.toString)
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
    Right(GotValue[List[A]](list)).asInstanceOf[ApiResult[List[A]]]
}
