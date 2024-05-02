package flightdatabase

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeError
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate._
import flightdatabase.domain._
import flightdatabase.repository.queries._
import flightdatabase.utils.FieldValue
import flightdatabase.utils.implicits.enrichOption
import flightdatabase.utils.implicits.enrichQuery

import java.sql.SQLException

package object repository {

  // Helper methods to access DB
  def getFieldList[S: TableBase, V: Read](field: String): ConnectionIO[ApiResult[List[V]]] =
    selectFragment[S](field).query[V].asList(Some(field))

  def getFieldList[ST: TableBase, SV: Read, WT: TableBase, WV: Put](
    selectField: String,
    whereFieldValue: FieldValue[WT, WV],
    maybeIdField: Option[String] = None
  ): ConnectionIO[ApiResult[List[SV]]] = {
    val selectTable = implicitly[TableBase[ST]].asString
    val whereTable = whereFieldValue.table
    val idField = maybeIdField.getOrElse(s"${whereTable}_id")
    for {
      index <- selectWhereQuery[WT, Long, WV]("id", whereFieldValue.field, whereFieldValue.value).option.attempt
      values <- index match {
        case Right(Some(id)) =>
          selectWhereQuery[ST, SV, Long](selectField, idField, id).asList(Some(selectField))
        case Right(None) => EntryNotFound(whereFieldValue).elevate[ConnectionIO, List[SV]]
        case Left(error: SQLException) =>
          sqlStateToApiError(
            SqlState(error.getSQLState),
            Some(whereFieldValue.field),
            Some(whereFieldValue.value)
          ).elevate[ConnectionIO, List[SV]]
        case Left(error) =>
          UnknownDbError(error.getLocalizedMessage).elevate[ConnectionIO, List[SV]]
      }
    } yield values
  }

  def featureNotImplemented[F[_]: Applicative, A]: F[ApiResult[A]] =
    FeatureNotImplemented.elevate[F, A]

  // SQL state to ApiError conversion
  def sqlStateToApiError(
    state: SqlState,
    invalidField: Option[String] = None,
    invalidValue: Option[_] = None
  ): ApiError = state match {
    case class23.CHECK_VIOLATION       => EntryCheckFailed
    case class23.NOT_NULL_VIOLATION    => EntryNullCheckFailed
    case class23.UNIQUE_VIOLATION      => EntryAlreadyExists
    case class23.FOREIGN_KEY_VIOLATION => EntryHasInvalidForeignKey
    case class42.UNDEFINED_COLUMN      => InvalidField(invalidField.debug)
    case class42.UNDEFINED_FUNCTION    => InvalidValueType(invalidValue.debug)
    case _                             => SqlError(state.value)
  }
}
