package flightdatabase.domain.city

import doobie.Put
import flightdatabase.domain.ApiResult

trait CityAlgebra[F[_]] {
  def getCities: F[ApiResult[List[City]]]
  def getCitiesOnlyNames: F[ApiResult[List[String]]]
  def getCity(id: Long): F[ApiResult[City]]
  def getCities[V: Put](field: String, value: V): F[ApiResult[List[City]]]
  def getCitiesByCountry(country: String): F[ApiResult[List[City]]]
  def createCity(city: CityCreate): F[ApiResult[Long]]
  def updateCity(city: City): F[ApiResult[City]]
  def partiallyUpdateCity(id: Long, patch: CityPatch): F[ApiResult[City]]
  def removeCity(id: Long): F[ApiResult[Unit]]
}
