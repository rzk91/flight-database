package flightdatabase.domain.airplane

import flightdatabase.domain.ApiResult

trait AirplaneAlgebra[F[_]] {
  def getAirplanes(manufacturer: Option[String]): F[ApiResult[List[AirplaneModel]]]
  def getAirplanesOnlyNames(manufacturer: Option[String]): F[ApiResult[List[String]]]
  def getAirplane(id: Long): F[ApiResult[AirplaneModel]]
  def createAirplane(airplane: AirplaneModel): F[ApiResult[Long]]
  def updateAirplane(airplane: AirplaneModel): F[ApiResult[AirplaneModel]]
  def deleteAirplane(id: Long): F[ApiResult[AirplaneModel]]
}
