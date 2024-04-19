package flightdatabase.domain.fleet_route

import doobie.Put
import flightdatabase.domain.ApiResult

trait FleetRouteAlgebra[F[_]] {
  def doesFleetRouteExist(id: Long): F[Boolean]
  def getFleetRoutes: F[ApiResult[List[FleetRoute]]]
  def getFleetRoutesOnlyRoutes: F[ApiResult[List[String]]]
  def getFleetRoute(id: Long): F[ApiResult[FleetRoute]]
  def getFleetRoutes[V: Put](field: String, value: V): F[ApiResult[List[FleetRoute]]]
  def getFleetRoutesByFleetId(fleetId: Long): F[ApiResult[List[FleetRoute]]]
  def getFleetRoutesByAirplaneId(airplaneId: Long): F[ApiResult[List[FleetRoute]]]

  def getFleetRoutesByAirport[V: Put](
    field: String,
    value: V,
    inbound: Option[Boolean]
  ): F[ApiResult[List[FleetRoute]]]
  def createFleetRoute(fleetRoute: FleetRouteCreate): F[ApiResult[Long]]
  def updateFleetRoute(fleetRoute: FleetRoute): F[ApiResult[FleetRoute]]
  def partiallyUpdateFleetRoute(id: Long, patch: FleetRoutePatch): F[ApiResult[FleetRoute]]
  def removeFleetRoute(id: Long): F[ApiResult[Unit]]
}
