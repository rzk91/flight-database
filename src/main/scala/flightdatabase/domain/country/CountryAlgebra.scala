package flightdatabase.domain.country

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait CountryAlgebra[F[_]] {

  def doesCountryExist(id: Long): F[Boolean]
  def getCountries: F[ApiResult[Nel[Country]]]
  def getCountriesOnlyNames: F[ApiResult[Nel[String]]]
  def getCountry(id: Long): F[ApiResult[Country]]

  def getCountriesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Country]]]

  def getCountriesByLanguage[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Country]]]

  def getCountriesByCurrency[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Country]]]

  def createCountry(country: CountryCreate): F[ApiResult[Long]]
  def updateCountry(country: Country): F[ApiResult[Long]]
  def partiallyUpdateCountry(id: Long, patch: CountryPatch): F[ApiResult[Country]]
  def removeCountry(id: Long): F[ApiResult[Unit]]
}
