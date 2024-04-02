package flightdatabase.domain.fleet

import flightdatabase.domain.ApiResult

trait FleetAlgebra[F[_]] {
  def getFleet(airline: Option[String]): F[ApiResult[List[FleetModel]]]
  def getFleetOnlyNames(airline: Option[String]): F[ApiResult[List[String]]]
  def getFleetById(id: Long): F[ApiResult[FleetModel]]
  def createFleet(fleet: FleetModel): F[ApiResult[Long]]
  def updateFleet(fleet: FleetModel): F[ApiResult[FleetModel]]
  def deleteFleet(id: Long): F[ApiResult[FleetModel]]
}
