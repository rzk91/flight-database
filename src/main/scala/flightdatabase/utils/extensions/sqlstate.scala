package flightdatabase.utils.extensions

import cats.data.{NonEmptyList => Nel}
import doobie.SqlState
import doobie.postgres.sqlstate._
import flightdatabase.domain.{
  ApiError,
  EntryAlreadyExists,
  EntryCheckFailed,
  EntryHasInvalidForeignKey,
  EntryNullCheckFailed,
  InvalidField,
  InvalidValueType,
  SqlError
}
import flightdatabase.utils.extensions.option._

final class SqlStateOps(private val state: SqlState) {

  def toApiError(
    invalidField: Option[String] = None,
    invalidValues: Option[Nel[_]] = None
  ): ApiError =
    state match {
      case class23.CHECK_VIOLATION       => EntryCheckFailed
      case class23.NOT_NULL_VIOLATION    => EntryNullCheckFailed
      case class23.UNIQUE_VIOLATION      => EntryAlreadyExists
      case class23.FOREIGN_KEY_VIOLATION => EntryHasInvalidForeignKey
      case class42.UNDEFINED_COLUMN      => InvalidField(invalidField.debug)
      case class42.UNDEFINED_FUNCTION =>
        InvalidValueType(invalidValues.map(_.toList.mkString(", ")).debug)
      case _ => SqlError(state.value)
    }
}

trait ToSqlStateOps {
  @inline implicit def toSqlStateOps(state: SqlState): SqlStateOps = new SqlStateOps(state)
}

object sqlstate extends ToSqlStateOps
