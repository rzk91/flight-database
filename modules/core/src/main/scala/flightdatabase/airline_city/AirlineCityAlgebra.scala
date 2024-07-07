package flightdatabase.airline_city

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.ApiResult
import flightdatabase.Operator
import flightdatabase.TableBase

trait AirlineCityAlgebra[F[_]] {
  def doesAirlineCityExist(id: Long): F[Boolean]
  def getAirlineCities: F[ApiResult[Nel[AirlineCity]]]
  def getAirlineCity(id: Long): F[ApiResult[AirlineCity]]
  def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]]

  def getAirlineCitiesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineCity]]]

  def getAirlineCitiesByExternal[ET: TableBase, EV: Put](
    field: String,
    values: Nel[EV],
    operator: Operator
  ): F[ApiResult[Nel[AirlineCity]]]

  def getAirlineCitiesByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineCity]]]

  def getAirlineCitiesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineCity]]]

  def createAirlineCity(airlineCity: AirlineCityCreate): F[ApiResult[Long]]
  def updateAirlineCity(airlineCity: AirlineCity): F[ApiResult[Long]]

  def partiallyUpdateAirlineCity(
    id: Long,
    patch: AirlineCityPatch
  ): F[ApiResult[AirlineCity]]

  def removeAirlineCity(id: Long): F[ApiResult[Unit]]
}
