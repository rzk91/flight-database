package flightdatabase

import cats.Applicative
import cats.syntax.applicativeError._
import doobie._
import doobie.implicits._
import doobie.postgres._
import flightdatabase.domain._
import flightdatabase.repository.queries._
import flightdatabase.utils.FieldValue
import flightdatabase.utils.implicits.enrichQuery

package object repository {

  // Helper methods to access DB
  def getFieldList[S: TableBase, V: Read](field: String): ConnectionIO[ApiResult[List[V]]] =
    selectFragment[S](field).query[V].asList

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
        case Right(Some(id)) => selectWhereQuery[ST, SV, Long](selectField, idField, id).asList
        case Right(None)     => EntryNotFound(whereFieldValue).elevate[ConnectionIO, List[SV]]
        case Left(error)     => UnknownDbError(error.getMessage).elevate[ConnectionIO, List[SV]]
      }
    } yield values
  }

  def featureNotImplemented[F[_]: Applicative, A]: F[ApiResult[A]] =
    FeatureNotImplemented.elevate[F, A]

  // SQL state to ApiError conversion
  def sqlStateToApiError(state: SqlState): ApiError = state match {
    case sqlstate.class23.CHECK_VIOLATION       => EntryCheckFailed
    case sqlstate.class23.NOT_NULL_VIOLATION    => EntryNullCheckFailed
    case sqlstate.class23.UNIQUE_VIOLATION      => EntryAlreadyExists
    case sqlstate.class23.FOREIGN_KEY_VIOLATION => EntryHasInvalidForeignKey
    case _                                      => UnknownDbError(state.value)
  }
}
