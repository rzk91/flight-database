package flightdatabase.utils.implicits

import doobie.ConnectionIO
import doobie.Query0
import flightdatabase.domain.ApiResult
import flightdatabase.repository.liftListToApiResult

class RichQuery[A](private val q: Query0[A]) extends AnyVal {
  def asList: ConnectionIO[ApiResult[List[A]]] = q.to[List].map(liftListToApiResult)
  // TODO: Add .asStream
}
