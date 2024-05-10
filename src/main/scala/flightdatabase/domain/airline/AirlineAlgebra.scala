package flightdatabase.domain.airline

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait AirlineAlgebra[F[_]] {
  def doesAirlineExist(id: Long): F[Boolean]
  def getAirlines: F[ApiResult[List[Airline]]]
  def getAirlinesOnlyNames: F[ApiResult[List[String]]]
  def getAirline(id: Long): F[ApiResult[Airline]]

  def getAirlinesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Airline]]]

  def getAirlinesByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Airline]]]

  def createAirline(airline: AirlineCreate): F[ApiResult[Long]]
  def updateAirline(airline: Airline): F[ApiResult[Long]]
  def partiallyUpdateAirline(id: Long, patch: AirlinePatch): F[ApiResult[Airline]]
  def removeAirline(id: Long): F[ApiResult[Unit]]
}
