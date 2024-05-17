package flightdatabase.domain.airline

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.ValidatedSortAndLimit

trait AirlineAlgebra[F[_]] {
  def doesAirlineExist(id: Long): F[Boolean]
  def getAirlines(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Airline]]]

  def getAirlinesOnly[V: Read](
    sortAndLimit: ValidatedSortAndLimit,
    returnField: String
  ): F[ApiResult[Nel[V]]]
  def getAirline(id: Long): F[ApiResult[Airline]]

  def getAirlinesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airline]]]

  def getAirlinesByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airline]]]

  def createAirline(airline: AirlineCreate): F[ApiResult[Long]]
  def updateAirline(airline: Airline): F[ApiResult[Long]]
  def partiallyUpdateAirline(id: Long, patch: AirlinePatch): F[ApiResult[Airline]]
  def removeAirline(id: Long): F[ApiResult[Unit]]
}
