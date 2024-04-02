package flightdatabase.domain.fleet_airplane

import flightdatabase.domain.ApiResult

trait FleetAirplaneAlgebra[F[_]] {
  def getFleetAirplanes: F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplaneById(id: Long): F[ApiResult[FleetAirplaneModel]]
  def getFleetAirplanesByAirplaneId(airplaneId: Long): F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplanesByFleetId(fleetId: Long): F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplanesByAirplaneName(airplaneName: String): F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplanesByFleetName(fleetName: String): F[ApiResult[List[FleetAirplaneModel]]]
  def createFleetAirplane(fleetAirplane: FleetAirplaneModel): F[ApiResult[Long]]
  def updateFleetAirplane(fleetAirplane: FleetAirplaneModel): F[ApiResult[FleetAirplaneModel]]
  def deleteFleetAirplane(id: Long): F[ApiResult[FleetAirplaneModel]]
}
