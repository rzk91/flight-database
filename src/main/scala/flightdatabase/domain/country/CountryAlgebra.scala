package flightdatabase.domain.country

import flightdatabase.domain.ApiResult

trait CountryAlgebra[F[_]] {
  def getCountries: F[ApiResult[List[CountryModel]]]
  def getCountriesOnlyNames: F[ApiResult[List[String]]]
  def getCountry(id: Int): F[ApiResult[CountryModel]]
  def createCountry(country: CountryModel): F[ApiResult[Int]]
  def updateCountry(country: CountryModel): F[ApiResult[CountryModel]]
  def removeCountry(id: Int): F[ApiResult[Unit]]
}
