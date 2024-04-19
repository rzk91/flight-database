package flightdatabase.domain.country

import doobie.Put
import flightdatabase.domain.ApiResult

trait CountryAlgebra[F[_]] {

  def doesCountryExist(id: Long): F[Boolean]
  def getCountries: F[ApiResult[List[Country]]]
  def getCountriesOnlyNames: F[ApiResult[List[String]]]
  def getCountry(id: Long): F[ApiResult[Country]]
  def getCountries[V: Put](field: String, value: V): F[ApiResult[List[Country]]]
  def createCountry(country: CountryCreate): F[ApiResult[Long]]
  def updateCountry(country: Country): F[ApiResult[Country]]
  def partiallyUpdateCountry(id: Long, patch: CountryPatch): F[ApiResult[Country]]
  def removeCountry(id: Long): F[ApiResult[Unit]]
}
