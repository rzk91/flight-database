package flightdatabase.domain.airplane

import flightdatabase.domain.ApiResult

trait AirplaneAlgebra[F[_]] {
  def getAirplanes: F[ApiResult[List[AirplaneModel]]]
  def getAirplanesOnlyNames: F[ApiResult[List[String]]]
  def getAirplane(id: Long): F[ApiResult[AirplaneModel]]
  def getAirplanesByManufacturer(manufacturer: String): F[ApiResult[List[AirplaneModel]]]
  def createAirplane(airplane: AirplaneModel): F[ApiResult[Long]]
  def updateAirplane(airplane: AirplaneModel): F[ApiResult[AirplaneModel]]
  def removeAirplane(id: Long): F[ApiResult[Unit]]
}
