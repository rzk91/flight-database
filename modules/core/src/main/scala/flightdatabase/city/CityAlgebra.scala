package flightdatabase.city

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait CityAlgebra[F[_]] {

  def doesCityExist(id: Long): F[Boolean]
  def getCities: PartiallyAppliedGetAll[F, City]
  def getCity(id: Long): F[ApiResult[City]]
  def getCitiesBy: PartiallyAppliedGetBy[F, City]
  def getCitiesByCountry: PartiallyAppliedGetBy[F, City]
  def createCity(city: CityCreate): F[ApiResult[Long]]
  def updateCity(city: City): F[ApiResult[Long]]
  def partiallyUpdateCity(id: Long, patch: CityPatch): F[ApiResult[City]]
  def removeCity(id: Long): F[ApiResult[Unit]]

  protected def validateTimezone(timezone: String, countryId: Long): F[ApiResult[Unit]]
}
