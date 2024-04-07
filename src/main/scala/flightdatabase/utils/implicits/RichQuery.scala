package flightdatabase.utils.implicits

import doobie.ConnectionIO
import doobie.Query0
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryNotFound
import flightdatabase.repository.liftErrorToApiResult
import flightdatabase.repository.liftListToApiResult
import flightdatabase.repository.liftToApiResult

class RichQuery[A](private val q: Query0[A]) extends AnyVal {
  def asList: ConnectionIO[ApiResult[List[A]]] = q.to[List].map(liftListToApiResult)

  def asSingle: ConnectionIO[ApiResult[A]] =
    q.option.map(_.fold(liftErrorToApiResult[A](EntryNotFound))(liftToApiResult))

  // TODO: Add .asStream
}
