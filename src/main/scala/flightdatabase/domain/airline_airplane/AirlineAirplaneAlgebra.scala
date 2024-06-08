package flightdatabase.domain.airline_airplane

import flightdatabase.domain.ApiResult
import flightdatabase.domain.partial.{PartiallyAppliedGetAll, PartiallyAppliedGetBy}

trait AirlineAirplaneAlgebra[F[_]] {
  def doesAirlineAirplaneExist(id: Long): F[Boolean]
  def getAirlineAirplanes: PartiallyAppliedGetAll[F, AirlineAirplane]
  def getAirlineAirplane(id: Long): F[ApiResult[AirlineAirplane]]
  def getAirlineAirplane(airlineId: Long, airplaneId: Long): F[ApiResult[AirlineAirplane]]
  def getAirlineAirplanesBy: PartiallyAppliedGetBy[F, AirlineAirplane]
  def getAirlineAirplanesByAirplane: PartiallyAppliedGetBy[F, AirlineAirplane]
  def getAirlineAirplanesByAirline: PartiallyAppliedGetBy[F, AirlineAirplane]
  def createAirlineAirplane(airlineAirplane: AirlineAirplaneCreate): F[ApiResult[Long]]
  def updateAirlineAirplane(airlineAirplane: AirlineAirplane): F[ApiResult[Long]]

  def partiallyUpdateAirlineAirplane(
    id: Long,
    patch: AirlineAirplanePatch
  ): F[ApiResult[AirlineAirplane]]

  def removeAirlineAirplane(id: Long): F[ApiResult[Unit]]
}
