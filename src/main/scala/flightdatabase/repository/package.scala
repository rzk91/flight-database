package flightdatabase

import cats.Applicative
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.Table
import flightdatabase.repository.queries._
import flightdatabase.utils.TableValue
import flightdatabase.utils.implicits.enrichQuery

package object repository {

  // Helper methods to access DB
  def getNameList[W](
    selectTable: Table,
    whereTableValue: Option[TableValue[W]] = None
  ): ConnectionIO[ApiResult[List[String]]] =
    whereTableValue match {
      case Some(TableValue(whereTable, whereValue)) =>
        // Get only names based on given value
        for {
          id <- selectWhereQuery[Int, W]("id", whereTable, "name", whereValue).option
          names <- id match {
            case Some(i) =>
              selectWhereQuery[String, Int]("name", selectTable, s"${whereTable}_id", i).asList
            case None => liftErrorToApiResult[List[String]](EntryNotFound).pure[ConnectionIO]
          }
        } yield names

      case None =>
        // Get all names in DB
        selectFragment("name", selectTable).query[String].asList
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
  def liftListToApiResult[A](list: List[A]): ApiResult[List[A]] =
    GotValue[List[A]](list).asRight[ApiError]

  def liftErrorToApiResult[A](error: ApiError): ApiResult[A] =
    error.asLeft[ApiOutput[A]]
}
