package flightdatabase.utils.extensions

import cats.implicits._
import doobie.ConnectionIO
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain._
import flightdatabase.utils.extensions.sqlstate._

final class UpdateOps(private val update: Update0) extends AnyVal {

  def attemptInsert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.bimap(_.toApiError(), Created(_)))

  def attemptUpdate[A](updated: A): ConnectionIO[ApiResult[A]] =
    update.run.attemptSqlState.map {
      case Left(sqlError) => sqlError.toApiError().asResult[A]
      case Right(0)       => EntryNotFound(updated).asResult[A]
      case _              => Updated(updated).asResult
    }

  def attemptDelete[E](entry: E): ConnectionIO[ApiResult[Unit]] =
    update.run.map {
      case 1 => Deleted.asResult
      case _ => EntryNotFound(entry).asResult[Unit]
    }
}

trait ToUpdateOps {
  @inline implicit def toUpdateOps(update: Update0): UpdateOps = new UpdateOps(update)
}

object update extends ToUpdateOps
