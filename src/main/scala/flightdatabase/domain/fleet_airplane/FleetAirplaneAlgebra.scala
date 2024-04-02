package flightdatabase.domain.fleet_airplane

trait FleetAirplaneAlgebra[F[_]] {
  def getFleetAirplanes: F[List[FleetAirplaneModel]]
  def getFleetAirplaneById(id: Long): F[Option[FleetAirplaneModel]]
  def getFleetAirplanesByAirplaneId(airplaneId: Long): F[List[FleetAirplaneModel]]
  def getFleetAirplanesByFleetId(fleetId: Long): F[List[FleetAirplaneModel]]
  def getFleetAirplanesByAirplaneName(airplaneName: String): F[List[FleetAirplaneModel]]
  def getFleetAirplanesByFleetName(fleetName: String): F[List[FleetAirplaneModel]]
  def createFleetAirplane(fleetAirplane: FleetAirplaneModel): F[Long]
  def updateFleetAirplane(fleetAirplane: FleetAirplaneModel): F[Option[FleetAirplaneModel]]
  def deleteFleetAirplane(id: Long): F[Option[FleetAirplaneModel]]
}
