package flightdatabase.airline_city

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait AirlineCityAlgebra[F[_]] {
  def doesAirlineCityExist(id: Long): F[Boolean]
  def getAirlineCities: PartiallyAppliedGetAll[F, AirlineCity]
  def getAirlineCity(id: Long): F[ApiResult[AirlineCity]]
  def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]]
  def getAirlineCitiesBy: PartiallyAppliedGetBy[F, AirlineCity]
  def getAirlineCitiesByCity: PartiallyAppliedGetBy[F, AirlineCity]
  def getAirlineCitiesByAirline: PartiallyAppliedGetBy[F, AirlineCity]
  def createAirlineCity(airlineCity: AirlineCityCreate): F[ApiResult[Long]]
  def updateAirlineCity(airlineCity: AirlineCity): F[ApiResult[Long]]

  def partiallyUpdateAirlineCity(
    id: Long,
    patch: AirlineCityPatch
  ): F[ApiResult[AirlineCity]]

  def removeAirlineCity(id: Long): F[ApiResult[Unit]]
}
