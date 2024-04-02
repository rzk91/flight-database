package flightdatabase.domain.country

import flightdatabase.domain.ApiResult

trait CountryAlgebra[F[_]] {
  def getCountries: F[ApiResult[List[CountryModel]]]
  def getCountriesOnlyNames: F[ApiResult[List[String]]]
  def getCountryById(id: Long): F[ApiResult[CountryModel]]
  def createCountry(country: CountryModel): F[ApiResult[Long]]
  def updateCountry(country: CountryModel): F[ApiResult[CountryModel]]
  def deleteCountry(id: Long): F[ApiResult[CountryModel]]
}
