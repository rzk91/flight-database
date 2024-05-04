package flightdatabase.domain.airline_city

import doobie.Put
import flightdatabase.domain.ApiResult
import flightdatabase.domain.TableBase

trait AirlineCityAlgebra[F[_]] {
  def doesAirlineCityExist(id: Long): F[Boolean]
  def getAirlineCities: F[ApiResult[List[AirlineCity]]]
  def getAirlineCity(id: Long): F[ApiResult[AirlineCity]]
  def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]]
  def getAirlineCities[V: Put](field: String, value: V): F[ApiResult[List[AirlineCity]]]

  def getAirlineCitiesByExternal[ET: TableBase, EV: Put](
    field: String,
    value: EV
  ): F[ApiResult[List[AirlineCity]]]
  def getAirlineCitiesByCity[V: Put](field: String, value: V): F[ApiResult[List[AirlineCity]]]
  def getAirlineCitiesByAirline[V: Put](field: String, value: V): F[ApiResult[List[AirlineCity]]]
  def createAirlineCity(airlineCity: AirlineCityCreate): F[ApiResult[Long]]
  def updateAirlineCity(airlineCity: AirlineCity): F[ApiResult[Long]]

  def partiallyUpdateAirlineCity(
    id: Long,
    patch: AirlineCityPatch
  ): F[ApiResult[AirlineCity]]
  def removeAirlineCity(id: Long): F[ApiResult[Unit]]
}
