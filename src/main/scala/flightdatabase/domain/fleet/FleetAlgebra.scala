package flightdatabase.domain.fleet

import doobie.Put
import flightdatabase.domain.ApiResult

trait FleetAlgebra[F[_]] {
  def doesFleetExist(id: Long): F[Boolean]
  def getFleets: F[ApiResult[List[Fleet]]]
  def getFleetsOnlyNames: F[ApiResult[List[String]]]
  def getFleet(id: Long): F[ApiResult[Fleet]]
  def getFleets[V: Put](field: String, value: V): F[ApiResult[List[Fleet]]]
  def getFleetByHubAirportIata(hubAirportIata: String): F[ApiResult[List[Fleet]]]
  def getFleetByHubAirportIcao(hubAirportIcao: String): F[ApiResult[List[Fleet]]]
  def createFleet(fleet: FleetCreate): F[ApiResult[Long]]
  def updateFleet(fleet: Fleet): F[ApiResult[Long]]
  def partiallyUpdateFleet(id: Long, patch: FleetPatch): F[ApiResult[Fleet]]
  def removeFleet(id: Long): F[ApiResult[Unit]]
}
