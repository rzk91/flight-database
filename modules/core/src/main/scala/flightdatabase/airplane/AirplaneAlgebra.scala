package flightdatabase.airplane

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait AirplaneAlgebra[F[_]] {
  def doesAirplaneExist(id: Long): F[Boolean]
  def getAirplanes: PartiallyAppliedGetAll[F, Airplane]
  def getAirplane(id: Long): F[ApiResult[Airplane]]
  def getAirplanesBy: PartiallyAppliedGetBy[F, Airplane]
  def getAirplanesByManufacturer: PartiallyAppliedGetBy[F, Airplane]
  def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]]
  def updateAirplane(airplane: Airplane): F[ApiResult[Long]]
  def partiallyUpdateAirplane(id: Long, patch: AirplanePatch): F[ApiResult[Airplane]]
  def removeAirplane(id: Long): F[ApiResult[Unit]]
}
