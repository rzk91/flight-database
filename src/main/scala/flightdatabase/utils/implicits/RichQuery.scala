package flightdatabase.utils.implicits

import cats.implicits._
import doobie.ConnectionIO
import doobie.Query0
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.UnknownError
import flightdatabase.repository.liftErrorToApiResult
import flightdatabase.repository.liftListToApiResult
import flightdatabase.repository.liftToApiResult
import flightdatabase.repository.sqlStateToApiError
import fs2.Stream

class RichQuery[A](private val q: Query0[A]) extends AnyVal {

  def asList: ConnectionIO[ApiResult[List[A]]] =
    q.to[List].attemptSqlState.map {
      case Right(list) => liftListToApiResult(list)
      case Left(error) => liftErrorToApiResult[List[A]](sqlStateToApiError(error))
    }

  def asSingle[E](entry: E): ConnectionIO[ApiResult[A]] =
    q.option.attempt.map {
      case Right(Some(a)) => liftToApiResult(a)
      case Right(None)    => liftErrorToApiResult[A](EntryNotFound(entry.toString))
      case Left(error)    => liftErrorToApiResult[A](UnknownError(error.getMessage))
    }

  def asStream: Stream[ConnectionIO, ApiResult[A]] = q.stream.map(liftToApiResult)
}
