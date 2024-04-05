package flightdatabase.utils.implicits

import doobie.ConnectionIO
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.CreatedValue
import flightdatabase.repository.sqlStateToApiError

class RichUpdate(private val update: Update0) extends AnyVal {

  def insert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, CreatedValue(_)))

}
