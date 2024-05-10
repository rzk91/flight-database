package flightdatabase

import cats.Applicative
import cats.data.{NonEmptyList => Nel}
import cats.syntax.applicativeError._
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate._
import flightdatabase.api.Operator
import flightdatabase.domain._
import flightdatabase.repository.queries._
import flightdatabase.utils.FieldValues
import flightdatabase.utils.implicits.enrichOption
import flightdatabase.utils.implicits.enrichQuery

import java.sql.SQLException

package object repository {

  // Helper methods to access DB
  def getFieldList[S: TableBase, V: Read](field: String): ConnectionIO[ApiResult[List[V]]] =
    selectFragment[S](field).query[V].asList(Some(field))

  def getFieldList[ST: TableBase, SV: Read, WT: TableBase, WV: Put](
    selectField: String,
    whereFieldValues: FieldValues[WT, WV],
    operator: Operator,
    maybeIdField: Option[String] = None
  ): ConnectionIO[ApiResult[List[SV]]] = {
    val selectTable = implicitly[TableBase[ST]].asString
    val whereTable = whereFieldValues.table
    val idField = maybeIdField.getOrElse(s"${whereTable}_id")
    for {
      index <- selectWhereQuery[WT, Long, WV](
        "id",
        whereFieldValues.field,
        whereFieldValues.values,
        operator
      ).option.attempt // TODO: This could be wrong. Check if this should be changed to `to[List]`.
      values <- index match {
        case Right(Some(id)) =>
          selectWhereQuery[ST, SV, Long](selectField, idField, Nel.one(id), Operator.Equals)
            .asList(Some(selectField))
        case Right(None) => EntryNotFound(whereFieldValues).elevate[ConnectionIO, List[SV]]
        case Left(error: SQLException) =>
          sqlStateToApiError(
            SqlState(error.getSQLState),
            Some(whereFieldValues.field),
            Some(whereFieldValues.values)
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
    invalidValues: Option[Nel[_]] = None
  ): ApiError = state match {
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
