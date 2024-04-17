package flightdatabase.domain.fleet_airplane

import doobie.Put
import flightdatabase.domain.ApiResult
import flightdatabase.domain.TableBase

trait FleetAirplaneAlgebra[F[_]] {
  def doesFleetAirplaneExist(id: Long): F[Boolean]
  def getFleetAirplanes: F[ApiResult[List[FleetAirplane]]]
  def getFleetAirplane(id: Long): F[ApiResult[FleetAirplane]]
  def getFleetAirplanes[V: Put](field: String, value: V): F[ApiResult[List[FleetAirplane]]]

  def getFleetAirplanesByExternal[ET: TableBase, EV: Put](
    field: String,
    value: EV
  ): F[ApiResult[List[FleetAirplane]]]
  def getFleetAirplanesByAirplaneName(airplaneName: String): F[ApiResult[List[FleetAirplane]]]
  def getFleetAirplanesByFleetName(fleetName: String): F[ApiResult[List[FleetAirplane]]]
  def createFleetAirplane(fleetAirplane: FleetAirplaneCreate): F[ApiResult[Long]]
  def updateFleetAirplane(fleetAirplane: FleetAirplane): F[ApiResult[FleetAirplane]]
  def partiallyUpdateFleetAirplane(id: Long, patch: FleetAirplanePatch): F[ApiResult[FleetAirplane]]
  def removeFleetAirplane(id: Long): F[ApiResult[Unit]]
}
