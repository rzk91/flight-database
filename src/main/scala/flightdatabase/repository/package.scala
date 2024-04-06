package flightdatabase

import cats.Applicative
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import flightdatabase.domain._
import flightdatabase.repository.queries._
import flightdatabase.utils.TableValue
import flightdatabase.utils.implicits.enrichQuery

package object repository {

  // Helper methods to access DB
  def getNameList[S: TableBase]: ConnectionIO[ApiResult[List[String]]] =
    selectFragment[S]("name").query[String].asList

  def getNameList[S: TableBase, WT: TableBase, WV: Put](
    whereTableValue: Option[TableValue[WT, WV]] = None
  ): ConnectionIO[ApiResult[List[String]]] =
    whereTableValue match {
      case Some(tv @ TableValue(whereValue)) =>
        // Get only names based on given value
        val whereTable = tv.asString
        for {
          id <- selectWhereQuery[WT, Int, WV]("id", "name", whereValue).option
          names <- id match {
            case Some(i) =>
              selectWhereQuery[S, String, Int]("name", s"${whereTable}_id", i).asList
            case None => liftErrorToApiResult[List[String]](EntryNotFound).pure[ConnectionIO]
          }
        } yield names

      case None =>
        // Get all names in DB
        getNameList[S]
    }

  def featureNotImplemented[F[_]: Applicative, A]: F[ApiResult[A]] =
    liftErrorToApiResult[A](FeatureNotImplemented).pure[F]

  // SQL state to ApiError conversion
  def sqlStateToApiError(state: SqlState): ApiError = state match {
    case sqlstate.class23.CHECK_VIOLATION    => EntryCheckFailed
    case sqlstate.class23.NOT_NULL_VIOLATION => EntryNullCheckFailed
    case sqlstate.class23.UNIQUE_VIOLATION   => EntryAlreadyExists
    case _                                   => UnknownError
  }

  // Lift to API Result
  def liftToApiResult[A](value: A): ApiResult[A] =
    GotValue[A](value).asRight[ApiError]

  def liftListToApiResult[A](list: List[A]): ApiResult[List[A]] =
    if (list.isEmpty) liftErrorToApiResult(EntryNotFound) else liftToApiResult(list)

  def liftErrorToApiResult[A](error: ApiError): ApiResult[A] =
    error.asLeft[ApiOutput[A]]
}
