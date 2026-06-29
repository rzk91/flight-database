package flightdatabase.persistence.syntax

import cats.syntax.bifunctor._
import flightdatabase.ApiResult
import flightdatabase.Created
import flightdatabase.Deleted
import flightdatabase.EntryNotFound
import flightdatabase.Updated
import flightdatabase.persistence.syntax.sqlstate._
import org.typelevel.doobie.ConnectionIO
import org.typelevel.doobie.Update0
import org.typelevel.doobie.syntax.applicativeerror._

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
