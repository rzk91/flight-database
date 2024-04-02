package flightdatabase.domain.fleet_route

trait FleetRouteAlgebra[F[_]] {
  def getFleetRoutes: F[List[FleetRouteModel]]
  def getFleetRouteById(id: Long): F[Option[FleetRouteModel]]
  def getFleetRoutesByRouteId(routeId: Long): F[List[FleetRouteModel]]
  def getFleetRoutesByRouteNumber(routeNumber: String): F[List[FleetRouteModel]]
  def getFleetRoutesByFleetId(fleetId: Long): F[List[FleetRouteModel]]
  def getFleetRoutesByFleetName(fleetName: String): F[List[FleetRouteModel]]
  def getInboundFleetRoutesByAirportId(airportId: Long): F[List[FleetRouteModel]]
  def getOutboundFleetRoutesByAirportId(airportId: Long): F[List[FleetRouteModel]]
  def getFleetRoutesByAirplaneId(airplaneId: Long): F[List[FleetRouteModel]]
  def createFleetRoute(fleetRoute: FleetRouteModel): F[Long]
  def updateFleetRoute(fleetRoute: FleetRouteModel): F[Option[FleetRouteModel]]
  def deleteFleetRoute(id: Long): F[Option[FleetRouteModel]]
}
