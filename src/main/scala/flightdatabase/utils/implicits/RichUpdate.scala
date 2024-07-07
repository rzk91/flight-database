package flightdatabase.utils.implicits

import cats.implicits._
import doobie.ConnectionIO
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain._
import flightdatabase.repository.sqlStateToApiError

final class RichUpdate(private val update: Update0) extends AnyVal {

  def attemptInsert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.bimap(sqlStateToApiError(_), Created(_)))

  def attemptUpdate[A](updated: A): ConnectionIO[ApiResult[A]] =
    update.run.attemptSqlState.map {
      case Left(sqlError) => sqlStateToApiError(sqlError).asResult[A]
      case Right(0)       => EntryNotFound(updated).asResult[A]
      case _              => Updated(updated).asResult
    }

  def attemptDelete[E](entry: E): ConnectionIO[ApiResult[Unit]] =
    update.run.map {
      case 1 => Deleted.asResult
      case _ => EntryNotFound(entry).asResult[Unit]
    }

}
