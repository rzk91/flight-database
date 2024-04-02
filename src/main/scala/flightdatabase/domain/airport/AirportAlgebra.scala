package flightdatabase.domain.airport

import flightdatabase.domain.ApiResult

trait AirportAlgebra[F[_]] {
  def getAirports: F[ApiResult[List[AirportModel]]]
  def getAirportsOnlyNames: F[ApiResult[List[String]]]
  def getAirportById(id: Long): F[ApiResult[AirportModel]]
  def getAirportByIata(iata: String): F[ApiResult[AirportModel]]
  def getAirportByIcao(icao: String): F[ApiResult[AirportModel]]
  def getAirportByCity(city: String): F[ApiResult[List[AirportModel]]]
  def getAirportByCountry(country: String): F[ApiResult[List[AirportModel]]]
  def createAirport(airport: AirportModel): F[ApiResult[Long]]
  def updateAirport(airport: AirportModel): F[ApiResult[AirportModel]]
  def deleteAirport(id: Long): F[ApiResult[AirportModel]]
}
