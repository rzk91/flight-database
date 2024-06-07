package flightdatabase.domain.airline

import flightdatabase.domain.ApiResult
import flightdatabase.domain.partial.PartiallyAppliedGetAll
import flightdatabase.domain.partial.PartiallyAppliedGetBy

trait AirlineAlgebra[F[_]] {
  def doesAirlineExist(id: Long): F[Boolean]
  def getAirlines: PartiallyAppliedGetAll[F, Airline]
  def getAirline(id: Long): F[ApiResult[Airline]]
  def getAirlinesBy: PartiallyAppliedGetBy[F, Airline]
  def getAirlinesByCountry: PartiallyAppliedGetBy[F, Airline]
  def createAirline(airline: AirlineCreate): F[ApiResult[Long]]
  def updateAirline(airline: Airline): F[ApiResult[Long]]
  def partiallyUpdateAirline(id: Long, patch: AirlinePatch): F[ApiResult[Airline]]
  def removeAirline(id: Long): F[ApiResult[Unit]]
}
