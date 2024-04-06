package flightdatabase.domain.fleet_route

import flightdatabase.domain.ApiResult

trait FleetRouteAlgebra[F[_]] {
  def getFleetRoutes: F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoute(id: Int): F[ApiResult[FleetRouteModel]]
  def getFleetRoutesByRouteNumber(routeNumber: String): F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoutesByFleetName(fleetName: String): F[ApiResult[List[FleetRouteModel]]]
  def getInboundFleetRoutesByAirportId(airportId: Int): F[ApiResult[List[FleetRouteModel]]]
  def getOutboundFleetRoutesByAirportId(airportId: Int): F[ApiResult[List[FleetRouteModel]]]
  def getFleetRoutesByAirplaneId(airplaneId: Int): F[ApiResult[List[FleetRouteModel]]]
  def createFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[Int]]
  def updateFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[FleetRouteModel]]
  def removeFleetRoute(id: Int): F[ApiResult[Unit]]
}
