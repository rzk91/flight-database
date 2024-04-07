package flightdatabase.domain.fleet_airplane

import flightdatabase.domain.ApiResult

trait FleetAirplaneAlgebra[F[_]] {
  def getFleetAirplanes: F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplane(id: Long): F[ApiResult[FleetAirplaneModel]]
  def getFleetAirplanesByAirplaneName(airplaneName: String): F[ApiResult[List[FleetAirplaneModel]]]
  def getFleetAirplanesByFleetName(fleetName: String): F[ApiResult[List[FleetAirplaneModel]]]
  def createFleetAirplane(fleetAirplane: FleetAirplaneModel): F[ApiResult[Long]]
  def updateFleetAirplane(fleetAirplane: FleetAirplaneModel): F[ApiResult[FleetAirplaneModel]]
  def removeFleetAirplane(id: Long): F[ApiResult[Unit]]
}
