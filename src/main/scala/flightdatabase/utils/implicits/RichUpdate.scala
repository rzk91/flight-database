package flightdatabase.utils.implicits

import doobie.{ConnectionIO, Update0}
import doobie.implicits._
import flightdatabase.domain.{ApiResult, CreatedValue}
import flightdatabase.repository.sqlStateToApiError

class RichUpdate(private val update: Update0) extends AnyVal {

  def insert: ConnectionIO[ApiResult[Long]] =
    update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSqlState
      .map(_.foldMap(sqlStateToApiError, CreatedValue(_)))

}
