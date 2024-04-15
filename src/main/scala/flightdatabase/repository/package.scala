package flightdatabase

import cats.Applicative
import cats.implicits._
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
        case Right(None) =>
          liftErrorToApiResult[List[SV]](EntryNotFound(whereFieldValue.toString)).pure[ConnectionIO]
        case Left(error) =>
          liftErrorToApiResult[List[SV]](UnknownError(error.getMessage)).pure[ConnectionIO]
      }
    } yield values
  }

  def featureNotImplemented[F[_]: Applicative, A]: F[ApiResult[A]] =
    liftErrorToApiResult[A](FeatureNotImplemented).pure[F]

  // SQL state to ApiError conversion
  def sqlStateToApiError(state: SqlState): ApiError = state match {
    case sqlstate.class23.CHECK_VIOLATION    => EntryCheckFailed
    case sqlstate.class23.NOT_NULL_VIOLATION => EntryNullCheckFailed
    case sqlstate.class23.UNIQUE_VIOLATION   => EntryAlreadyExists
    case _                                   => UnknownError(state.value)
  }

  // Lift to API Result
  def liftToApiResult[A](value: A): ApiResult[A] =
    GotValue[A](value).asRight[ApiError]

  def liftListToApiResult[A](list: List[A]): ApiResult[List[A]] =
    if (list.isEmpty) liftErrorToApiResult(EntryListEmpty) else liftToApiResult(list)

  def liftErrorToApiResult[A](error: ApiError): ApiResult[A] =
    error.asLeft[ApiOutput[A]]
}
