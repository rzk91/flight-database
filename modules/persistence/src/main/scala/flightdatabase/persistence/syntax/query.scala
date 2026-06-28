package flightdatabase.persistence.syntax

import cats.data.{NonEmptyList => Nel}
import cats.syntax.applicativeError._
import org.typelevel.doobie.ConnectionIO
import org.typelevel.doobie.Query0
import org.typelevel.doobie.enumerated.SqlState
import org.typelevel.doobie.implicits._
import org.typelevel.doobie.util.invariant.UnexpectedEnd
import flightdatabase.ApiResult
import flightdatabase.EntryListEmpty
import flightdatabase.EntryNotFound
import flightdatabase.Got
import flightdatabase.UnknownDbError
import flightdatabase.persistence.syntax.sqlstate._
import fs2.Stream

import java.sql.SQLException

final class QueryOps[A](private val q: Query0[A]) extends AnyVal {

  def asNel(
    invalidField: Option[String] = None,
    invalidValues: Option[Nel[_]] = None
  ): ConnectionIO[ApiResult[Nel[A]]] =
    q.nel.attempt.map {
      case Right(nel) => Got(nel).asResult
      case Left(error: SQLException) =>
        SqlState(error.getSQLState)
          .toApiError(invalidField, invalidValues)
          .asResult[Nel[A]]
      case Left(UnexpectedEnd) => EntryListEmpty.asResult[Nel[A]]
      case Left(error)         => UnknownDbError(error.getLocalizedMessage).asResult[Nel[A]]
    }

  def asSingle[E](entry: E): ConnectionIO[ApiResult[A]] =
    q.option.attempt.map {
      case Right(Some(a)) => Got(a).asResult
      case Right(None)    => EntryNotFound(entry).asResult[A]
      case Left(error: SQLException) =>
        SqlState(error.getSQLState)
          .toApiError(invalidValues = Some(Nel.one(entry)))
          .asResult[A]
      case Left(error) => UnknownDbError(error.getLocalizedMessage).asResult[A]
    }

  def asStream: Stream[ConnectionIO, ApiResult[A]] = q.stream.map(Got(_).asResult)
}

trait ToQueryOps {
  @inline implicit def toQueryOps[A](q: Query0[A]): QueryOps[A] = new QueryOps(q)
}

object query extends ToQueryOps
