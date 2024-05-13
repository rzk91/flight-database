package flightdatabase.domain.airline_route

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait AirlineRouteAlgebra[F[_]] {
  def doesAirlineRouteExist(id: Long): F[Boolean]
  def getAirlineRoutes: F[ApiResult[Nel[AirlineRoute]]]
  def getAirlineRoutesOnlyRoutes: F[ApiResult[Nel[String]]]
  def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]]

  def getAirlineRoutesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]]

  def getAirlineRoutesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]]

  def getAirlineRoutesByAirplane[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]]

  def getAirlineRoutesByAirport[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    inbound: Option[Boolean]
  ): F[ApiResult[Nel[AirlineRoute]]]

  def createAirlineRoute(airlineRoute: AirlineRouteCreate): F[ApiResult[Long]]
  def updateAirlineRoute(airlineRoute: AirlineRoute): F[ApiResult[Long]]
  def partiallyUpdateAirlineRoute(id: Long, patch: AirlineRoutePatch): F[ApiResult[AirlineRoute]]
  def removeAirlineRoute(id: Long): F[ApiResult[Unit]]
}
