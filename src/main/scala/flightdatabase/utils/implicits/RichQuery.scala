package flightdatabase.utils.implicits

import cats.implicits._
import doobie.ConnectionIO
import doobie.Query0
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.UnknownError
import flightdatabase.domain.listToApiResult
import flightdatabase.domain.toApiResult
import flightdatabase.repository.sqlStateToApiError
import fs2.Stream

class RichQuery[A](private val q: Query0[A]) extends AnyVal {

  def asList: ConnectionIO[ApiResult[List[A]]] =
    q.to[List].attemptSqlState.map {
      case Right(list) => listToApiResult(list)
      case Left(error) => sqlStateToApiError(error).asResult[List[A]]
    }

  def asSingle[E](entry: E): ConnectionIO[ApiResult[A]] =
    q.option.attempt.map {
      case Right(Some(a)) => toApiResult(a)
      case Right(None)    => EntryNotFound(entry).asResult[A]
      case Left(error)    => UnknownError(error.getMessage).asResult[A]
    }

  def asStream: Stream[ConnectionIO, ApiResult[A]] = q.stream.map(toApiResult)
}
