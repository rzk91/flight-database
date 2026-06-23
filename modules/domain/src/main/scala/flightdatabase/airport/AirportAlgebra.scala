package flightdatabase.airport

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait AirportAlgebra[F[_]] {
  def doesAirportExist(id: Long): F[Boolean]
  def getAirports: PartiallyAppliedGetAll[F, Airport]
  def getAirport(id: Long): F[ApiResult[Airport]]
  def getAirportsBy: PartiallyAppliedGetBy[F, Airport]
  def getAirportsByCity: PartiallyAppliedGetBy[F, Airport]
  def getAirportsByCountry: PartiallyAppliedGetBy[F, Airport]
  def createAirport(airport: AirportCreate): F[ApiResult[Long]]
  def updateAirport(airport: Airport): F[ApiResult[Long]]
  def partiallyUpdateAirport(id: Long, patch: AirportPatch): F[ApiResult[Airport]]
  def removeAirport(id: Long): F[ApiResult[Unit]]
}
