package flightdatabase.utils.implicits

import cats.implicits._
import doobie.ConnectionIO
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain._
import flightdatabase.repository.liftErrorToApiResult
import flightdatabase.repository.sqlStateToApiError

class RichUpdate(private val update: Update0) extends AnyVal {

  def attemptInsert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, Created(_)))

  def attemptUpdate[A](updated: A)(implicit T: TableBase[A]): ConnectionIO[ApiResult[A]] =
    update.run.attemptSqlState
      .map(_.foldMap(sqlStateToApiError, _ => Updated(updated)))

  def attemptDelete[E](entry: E): ConnectionIO[ApiResult[Unit]] =
    update.run.map {
      case 1 => Deleted.asRight[ApiError]
      case _ => liftErrorToApiResult(EntryNotFound(entry.toString))
    }

}
