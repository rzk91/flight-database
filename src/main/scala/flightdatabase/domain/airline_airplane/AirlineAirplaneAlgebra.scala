package flightdatabase.domain.airline_airplane

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.TableBase

trait AirlineAirplaneAlgebra[F[_]] {
  def doesAirlineAirplaneExist(id: Long): F[Boolean]
  def getAirlineAirplanes: F[ApiResult[Nel[AirlineAirplane]]]
  def getAirlineAirplane(id: Long): F[ApiResult[AirlineAirplane]]
  def getAirlineAirplane(airlineId: Long, airplaneId: Long): F[ApiResult[AirlineAirplane]]

  def getAirlineAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineAirplane]]]

  def getAirlineAirplanesByExternal[ET: TableBase, EV: Put](
    field: String,
    values: Nel[EV],
    operator: Operator
  ): F[ApiResult[Nel[AirlineAirplane]]]

  def getAirlineAirplanesByAirplane[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineAirplane]]]

  def getAirlineAirplanesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineAirplane]]]

  def createAirlineAirplane(airlineAirplane: AirlineAirplaneCreate): F[ApiResult[Long]]
  def updateAirlineAirplane(airlineAirplane: AirlineAirplane): F[ApiResult[Long]]

  def partiallyUpdateAirlineAirplane(
    id: Long,
    patch: AirlineAirplanePatch
  ): F[ApiResult[AirlineAirplane]]

  def removeAirlineAirplane(id: Long): F[ApiResult[Unit]]
}
