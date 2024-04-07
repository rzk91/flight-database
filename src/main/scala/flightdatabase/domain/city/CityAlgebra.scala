package flightdatabase.domain.city

import flightdatabase.domain.ApiResult

trait CityAlgebra[F[_]] {
  def getCities: F[ApiResult[List[CityModel]]]
  def getCitiesOnlyNames: F[ApiResult[List[String]]]
  def getCity(id: Long): F[ApiResult[CityModel]]
  def getCitiesByCountry(country: String): F[ApiResult[List[CityModel]]]
  def createCity(city: CityModel): F[ApiResult[Long]]
  def updateCity(city: CityModel): F[ApiResult[CityModel]]
  def removeCity(id: Long): F[ApiResult[Unit]]
}