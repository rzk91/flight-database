package flightdatabase.domain.airplane

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait AirplaneAlgebra[F[_]] {
  def doesAirplaneExist(id: Long): F[Boolean]
  def getAirplanes: F[ApiResult[List[Airplane]]]
  def getAirplanesOnlyNames: F[ApiResult[List[String]]]
  def getAirplane(id: Long): F[ApiResult[Airplane]]

  def getAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Airplane]]]

  def getAirplanesByManufacturer[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Airplane]]]

  def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]]
  def updateAirplane(airplane: Airplane): F[ApiResult[Long]]
  def partiallyUpdateAirplane(id: Long, patch: AirplanePatch): F[ApiResult[Airplane]]
  def removeAirplane(id: Long): F[ApiResult[Unit]]
}
