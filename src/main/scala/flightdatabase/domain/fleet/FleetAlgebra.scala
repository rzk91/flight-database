package flightdatabase.domain.fleet

import flightdatabase.domain.ApiResult

trait FleetAlgebra[F[_]] {
  def getFleets: F[ApiResult[List[FleetModel]]]
  def getFleetsOnlyNames: F[ApiResult[List[String]]]
  def getFleet(id: Int): F[ApiResult[FleetModel]]
  def getFleetByName(name: String): F[ApiResult[FleetModel]]
  def getFleetsByHub(hub: String): F[ApiResult[List[FleetModel]]]
  def createFleet(fleet: FleetModel): F[ApiResult[Int]]
  def updateFleet(fleet: FleetModel): F[ApiResult[FleetModel]]
  def removeFleet(id: Int): F[ApiResult[Unit]]
}
