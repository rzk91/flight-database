package flightdatabase.domain.airport

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait AirportAlgebra[F[_]] {
  def doesAirportExist(id: Long): F[Boolean]
  def getAirports: F[ApiResult[Nel[Airport]]]
  def getAirportsOnlyNames: F[ApiResult[Nel[String]]]
  def getAirport(id: Long): F[ApiResult[Airport]]

  def getAirportsBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]]

  def getAirportsByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]]

  def getAirportsByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]]

  def createAirport(airport: AirportCreate): F[ApiResult[Long]]
  def updateAirport(airport: Airport): F[ApiResult[Long]]
  def partiallyUpdateAirport(id: Long, patch: AirportPatch): F[ApiResult[Airport]]
  def removeAirport(id: Long): F[ApiResult[Unit]]
}
