package flightdatabase.domain.city

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait CityAlgebra[F[_]] {

  def doesCityExist(id: Long): F[Boolean]
  def getCities: F[ApiResult[Nel[City]]]
  def getCitiesOnly[V: Read](field: String): F[ApiResult[Nel[V]]]
  def getCity(id: Long): F[ApiResult[City]]

  def getCitiesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[City]]]

  def getCitiesByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[City]]]

  def createCity(city: CityCreate): F[ApiResult[Long]]
  def updateCity(city: City): F[ApiResult[Long]]
  def partiallyUpdateCity(id: Long, patch: CityPatch): F[ApiResult[City]]
  def removeCity(id: Long): F[ApiResult[Unit]]

  protected def validateTimezone(timezone: String, countryId: Long): F[ApiResult[Unit]]
}
