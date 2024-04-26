package flightdatabase.domain.airline

import doobie.Put
import flightdatabase.domain.ApiResult

trait AirlineAlgebra[F[_]] {
  def doesAirlineExist(id: Long): F[Boolean]
  def getAirlines: F[ApiResult[List[Airline]]]
  def getAirlinesOnlyNames: F[ApiResult[List[String]]]
  def getAirline(id: Long): F[ApiResult[Airline]]
  def getAirlines[V: Put](field: String, value: V): F[ApiResult[List[Airline]]]
  def getAirlinesByCountry[V: Put](field: String, value: V): F[ApiResult[List[Airline]]]
  def createAirline(airline: AirlineCreate): F[ApiResult[Long]]
  def updateAirline(airline: Airline): F[ApiResult[Long]]
  def partiallyUpdateAirline(id: Long, patch: AirlinePatch): F[ApiResult[Airline]]
  def removeAirline(id: Long): F[ApiResult[Unit]]
}
