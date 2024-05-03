package flightdatabase.domain.airline_route

import doobie.Put
import flightdatabase.domain.ApiResult

trait AirlineRouteAlgebra[F[_]] {
  def doesAirlineRouteExist(id: Long): F[Boolean]
  def getAirlineRoutes: F[ApiResult[List[AirlineRoute]]]
  def getAirlineRoutesOnlyRoutes: F[ApiResult[List[String]]]
  def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]]
  def getAirlineRoutes[V: Put](field: String, value: V): F[ApiResult[List[AirlineRoute]]]

  def getAirlineRoutesByAirline[V: Put](
    field: String,
    value: V
  ): F[ApiResult[List[AirlineRoute]]]

  def getAirlineRoutesByAirplane[V: Put](
    field: String,
    value: V
  ): F[ApiResult[List[AirlineRoute]]]

  def getAirlineRoutesByAirport[V: Put](
    field: String,
    value: V,
    inbound: Option[Boolean]
  ): F[ApiResult[List[AirlineRoute]]]

  def createAirlineRoute(airlineRoute: AirlineRouteCreate): F[ApiResult[Long]]
  def updateAirlineRoute(airlineRoute: AirlineRoute): F[ApiResult[Long]]
  def partiallyUpdateAirlineRoute(id: Long, patch: AirlineRoutePatch): F[ApiResult[AirlineRoute]]
  def removeAirlineRoute(id: Long): F[ApiResult[Unit]]
}
