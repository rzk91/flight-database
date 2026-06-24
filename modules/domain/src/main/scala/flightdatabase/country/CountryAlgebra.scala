package flightdatabase.country

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait CountryAlgebra[F[_]] {

  def doesCountryExist(id: Long): F[Boolean]
  def getCountries: PartiallyAppliedGetAll[F, Country]
  def getCountry(id: Long): F[ApiResult[Country]]
  def getCountriesBy: PartiallyAppliedGetBy[F, Country]
  def getCountriesByLanguage: PartiallyAppliedGetBy[F, Country]
  def getCountriesByCurrency: PartiallyAppliedGetBy[F, Country]
  def createCountry(country: CountryCreate): F[ApiResult[Long]]
  def updateCountry(country: Country): F[ApiResult[Long]]
  def partiallyUpdateCountry(id: Long, patch: CountryPatch): F[ApiResult[Country]]
  def removeCountry(id: Long): F[ApiResult[Unit]]
}
