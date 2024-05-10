package flightdatabase.domain.airline_city

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.TableBase

trait AirlineCityAlgebra[F[_]] {
  def doesAirlineCityExist(id: Long): F[Boolean]
  def getAirlineCities: F[ApiResult[List[AirlineCity]]]
  def getAirlineCity(id: Long): F[ApiResult[AirlineCity]]
  def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]]

  def getAirlineCitiesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]]

  def getAirlineCitiesByExternal[ET: TableBase, EV: Put](
    field: String,
    values: Nel[EV],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]]

  def getAirlineCitiesByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]]

  def getAirlineCitiesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]]

  def createAirlineCity(airlineCity: AirlineCityCreate): F[ApiResult[Long]]
  def updateAirlineCity(airlineCity: AirlineCity): F[ApiResult[Long]]

  def partiallyUpdateAirlineCity(
    id: Long,
    patch: AirlineCityPatch
  ): F[ApiResult[AirlineCity]]

  def removeAirlineCity(id: Long): F[ApiResult[Unit]]
}
