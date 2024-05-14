package flightdatabase.domain.airplane

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait AirplaneAlgebra[F[_]] {
  def doesAirplaneExist(id: Long): F[Boolean]
  def getAirplanes: F[ApiResult[Nel[Airplane]]]
  def getAirplanesOnly[V: Read](field: String): F[ApiResult[Nel[V]]]
  def getAirplane(id: Long): F[ApiResult[Airplane]]

  def getAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airplane]]]

  def getAirplanesByManufacturer[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airplane]]]

  def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]]
  def updateAirplane(airplane: Airplane): F[ApiResult[Long]]
  def partiallyUpdateAirplane(id: Long, patch: AirplanePatch): F[ApiResult[Airplane]]
  def removeAirplane(id: Long): F[ApiResult[Unit]]
}
