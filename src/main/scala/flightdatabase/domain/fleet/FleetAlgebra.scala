package flightdatabase.domain.fleet

import flightdatabase.domain.ApiResult

trait FleetAlgebra[F[_]] {
  def getFleets: F[ApiResult[List[FleetModel]]]
  def getFleetsOnlyNames: F[ApiResult[List[String]]]
  def getFleet(id: Long): F[ApiResult[FleetModel]]
  def getFleetByName(name: String): F[ApiResult[FleetModel]]
  def getFleetsByHub(hub: String): F[ApiResult[List[FleetModel]]]
  def createFleet(fleet: FleetModel): F[ApiResult[Long]]
  def updateFleet(fleet: FleetModel): F[ApiResult[FleetModel]]
  def removeFleet(id: Long): F[ApiResult[Unit]]
}
