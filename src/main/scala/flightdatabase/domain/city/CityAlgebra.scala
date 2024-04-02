package flightdatabase.domain.city

import flightdatabase.domain.ApiResult

trait CityAlgebra[F[_]] {
  def getCities(country: Option[String]): F[ApiResult[List[CityModel]]]
  def getCitiesOnlyNames(country: Option[String]): F[ApiResult[List[String]]]
  def getCityById(id: Long): F[ApiResult[CityModel]]
  def createCity(city: CityModel): F[ApiResult[Long]]
  def updateCity(city: CityModel): F[ApiResult[CityModel]]
  def deleteCity(id: Long): F[ApiResult[CityModel]]
}
