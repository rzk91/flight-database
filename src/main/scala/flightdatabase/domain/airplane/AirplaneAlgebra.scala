package flightdatabase.domain.airplane

import doobie.Put
import flightdatabase.domain.ApiResult

trait AirplaneAlgebra[F[_]] {
  def doesAirplaneExist(id: Long): F[Boolean]
  def getAirplanes: F[ApiResult[List[Airplane]]]
  def getAirplanesOnlyNames: F[ApiResult[List[String]]]
  def getAirplane(id: Long): F[ApiResult[Airplane]]
  def getAirplanes[V: Put](field: String, value: V): F[ApiResult[List[Airplane]]]
  def getAirplanesByManufacturer(manufacturer: String): F[ApiResult[List[Airplane]]]
  def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]]
  def updateAirplane(airplane: Airplane): F[ApiResult[Long]]
  def partiallyUpdateAirplane(id: Long, patch: AirplanePatch): F[ApiResult[Airplane]]
  def removeAirplane(id: Long): F[ApiResult[Unit]]
}
