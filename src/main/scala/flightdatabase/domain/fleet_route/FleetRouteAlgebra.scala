package flightdatabase.domain.fleet_route

import flightdatabase.domain.ApiResult

trait FleetRouteAlgebra[F[_]] {
  def getFleetRoutes: F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoute(id: Long): F[ApiResult[FleetRouteModel]]
  def getFleetRoutesByRouteNumber(routeNumber: String): F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoutesByFleetName(fleetName: String): F[ApiResult[List[FleetRouteModel]]]
  def getInboundFleetRoutesByAirportId(airportId: Long): F[ApiResult[List[FleetRouteModel]]]
  def getOutboundFleetRoutesByAirportId(airportId: Long): F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoutesByAirplaneId(airplaneId: Long): F[ApiResult[List[FleetRouteModel]]]
  def createFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[Long]]
  def updateFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[FleetRouteModel]]
  def removeFleetRoute(id: Long): F[ApiResult[Unit]]
}
