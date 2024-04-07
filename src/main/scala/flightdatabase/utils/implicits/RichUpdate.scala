package flightdatabase.utils.implicits

import doobie.ConnectionIO
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.CreatedValue
import flightdatabase.domain.EntryNotFound
import flightdatabase.repository.liftErrorToApiResult
import flightdatabase.repository.liftToApiResult
import flightdatabase.repository.sqlStateToApiError

class RichUpdate(private val update: Update0) extends AnyVal {

  def attemptInsert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, CreatedValue(_)))

  def attemptDelete: ConnectionIO[ApiResult[Unit]] =
    update.run.map {
      case 1 => liftToApiResult(())
      case _ => liftErrorToApiResult(EntryNotFound)
    }

}
