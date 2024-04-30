package flightdatabase.utils.implicits

import cats.syntax.applicativeError._
import doobie.ConnectionIO
import doobie.Query0
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.SqlError
import flightdatabase.domain.UnknownDbError
import flightdatabase.domain.listToApiResult
import flightdatabase.domain.toApiResult
import flightdatabase.repository.sqlStateToApiError
import fs2.Stream

import java.sql.SQLException

class RichQuery[A](private val q: Query0[A]) extends AnyVal {

  def asList: ConnectionIO[ApiResult[List[A]]] =
    q.to[List].attemptSqlState.map {
      case Right(list) => listToApiResult(list)
      case Left(error) => sqlStateToApiError(error).asResult[List[A]]
    }

  def asSingle[E](entry: E): ConnectionIO[ApiResult[A]] =
    q.option.attempt.map {
      case Right(Some(a))            => toApiResult(a)
      case Right(None)               => EntryNotFound(entry).asResult[A]
      case Left(error: SQLException) => SqlError(error.getSQLState).asResult[A]
      case Left(error)               => UnknownDbError(error.getLocalizedMessage).asResult[A]
    }

  def asStream: Stream[ConnectionIO, ApiResult[A]] = q.stream.map(toApiResult)
}
