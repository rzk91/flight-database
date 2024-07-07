package flightdatabase.utils.implicits

import cats.data.{NonEmptyList => Nel}
import cats.syntax.applicativeError._
import doobie.ConnectionIO
import doobie.Query0
import doobie.enumerated.SqlState
import doobie.implicits._
import doobie.util.invariant.UnexpectedEnd
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.Got
import flightdatabase.domain.UnknownDbError
import flightdatabase.repository.sqlStateToApiError
import fs2.Stream

import java.sql.SQLException

final class RichQuery[A](private val q: Query0[A]) extends AnyVal {

  def asNel(
    invalidField: Option[String] = None,
    invalidValues: Option[Nel[_]] = None
  ): ConnectionIO[ApiResult[Nel[A]]] =
    q.nel.attempt.map {
      case Right(nel) => Got(nel).asResult
      case Left(error: SQLException) =>
        sqlStateToApiError(SqlState(error.getSQLState), invalidField, invalidValues)
          .asResult[Nel[A]]
      case Left(UnexpectedEnd) => EntryListEmpty.asResult[Nel[A]]
      case Left(error)         => UnknownDbError(error.getLocalizedMessage).asResult[Nel[A]]
    }

  def asSingle[E](entry: E): ConnectionIO[ApiResult[A]] =
    q.option.attempt.map {
      case Right(Some(a)) => Got(a).asResult
      case Right(None)    => EntryNotFound(entry).asResult[A]
      case Left(error: SQLException) =>
        sqlStateToApiError(SqlState(error.getSQLState), invalidValues = Some(Nel.one(entry)))
          .asResult[A]
      case Left(error) => UnknownDbError(error.getLocalizedMessage).asResult[A]
    }

  def asStream: Stream[ConnectionIO, ApiResult[A]] = q.stream.map(Got(_).asResult)
}
