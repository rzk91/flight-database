package flightdatabase.domain.airport

import doobie.Put
import flightdatabase.domain.ApiResult

trait AirportAlgebra[F[_]] {
  def doesAirportExist(id: Long): F[Boolean]
  def getAirports: F[ApiResult[List[Airport]]]
  def getAirportsOnlyNames: F[ApiResult[List[String]]]
  def getAirport(id: Long): F[ApiResult[Airport]]
  def getAirportsBy[V: Put](field: String, value: V): F[ApiResult[List[Airport]]]
  def getAirportsByCity(city: String): F[ApiResult[List[Airport]]]
  def getAirportsByCountry(country: String): F[ApiResult[List[Airport]]]
  def createAirport(airport: AirportCreate): F[ApiResult[Long]]
  def updateAirport(airport: Airport): F[ApiResult[Airport]]
  def partiallyUpdateAirport(id: Long, patch: AirportPatch): F[ApiResult[Airport]]
  def removeAirport(id: Long): F[ApiResult[Unit]]
}
